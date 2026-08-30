from collections import deque
from pathlib import Path
from urllib.request import Request, urlopen
from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFilter, ImageOps


ROOT = Path(__file__).resolve().parents[1]
SIZE = 1254

PRODUCTS = [
    {
        "source_url": "https://cdn.fragrancenet.com/images/photos/1600x1600/493989.jpg",
        "path": ROOT / "src/main/resources/static/img/arabe/yara-candy.webp",
        "bg_top": (255, 244, 247),
        "bg_bottom": (245, 205, 216),
        "glow": (246, 82, 130),
        "threshold": 34,
        "padding": 72,
        "shadow": (128, 43, 63),
        "contrast": 1.06,
        "color": 1.08,
        "reflection": True,
        "mask_filter": "expand",
    },
    {
        "source_url": "https://shopforever.pk/wp-content/uploads/2021/09/7f5d1fad59df7895e15cdfe37fd6ff1f.jpg",
        "path": ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/burberry-her.webp",
        "bg_top": (239, 217, 222),
        "bg_bottom": (189, 145, 156),
        "glow": (255, 226, 231),
        "threshold": 14,
        "padding": 84,
        "shadow": (112, 70, 77),
        "contrast": 1.12,
        "color": 1.05,
        "reflection": False,
        "mask_filter": "expand",
    },
    {
        "source_url": "https://afnan.com/cdn/shop/files/Untitled_design_-_2024-12-31T113526.184.png?v=1735631782&width=2000",
        "path": ROOT / "src/main/resources/static/img/arabe/rebel-9pm.webp",
        "bg_top": (53, 48, 48),
        "bg_bottom": (18, 15, 16),
        "glow": (160, 24, 44),
        "threshold": 20,
        "padding": 92,
        "shadow": (6, 4, 5),
        "contrast": 1.11,
        "color": 1.08,
        "reflection": False,
        "mask_filter": "contract",
    },
]


def lerp(a, b, t):
    return int(a + (b - a) * t)


def gradient_background(size, top, bottom, glow):
    bg = Image.new("RGB", (size, size), top)
    px = bg.load()
    for y in range(size):
        t = y / (size - 1)
        for x in range(size):
            px[x, y] = tuple(lerp(top[i], bottom[i], t) for i in range(3))

    overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    cx, cy = size // 2, int(size * 0.42)
    for r in range(int(size * 0.68), 0, -18):
        alpha = int(78 * (1 - r / (size * 0.68)) ** 1.7)
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), fill=(*glow, alpha))
    return Image.alpha_composite(bg.convert("RGBA"), overlay)


def border_background_color(img):
    img = img.convert("RGB")
    samples = []
    w, h = img.size
    for x in range(0, w, max(1, w // 80)):
        samples.append(img.getpixel((x, 0)))
        samples.append(img.getpixel((x, h - 1)))
    for y in range(0, h, max(1, h // 80)):
        samples.append(img.getpixel((0, y)))
        samples.append(img.getpixel((w - 1, y)))
    return tuple(sorted(channel)[len(channel) // 2] for channel in zip(*samples))


def flood_background_mask(img, threshold):
    img = img.convert("RGB")
    w, h = img.size
    bg = border_background_color(img)
    visited = bytearray(w * h)
    mask = Image.new("L", (w, h), 0)
    mask_px = mask.load()
    px = img.load()
    q = deque()

    def add(x, y):
        idx = y * w + x
        if visited[idx]:
            return
        visited[idx] = 1
        color = px[x, y]
        dist = max(abs(color[i] - bg[i]) for i in range(3))
        if dist <= threshold:
            mask_px[x, y] = 255
            q.append((x, y))

    for x in range(w):
        add(x, 0)
        add(x, h - 1)
    for y in range(h):
        add(0, y)
        add(w - 1, y)

    while q:
        x, y = q.popleft()
        if x > 0:
            add(x - 1, y)
        if x < w - 1:
            add(x + 1, y)
        if y > 0:
            add(x, y - 1)
        if y < h - 1:
            add(x, y + 1)

    return mask


def source_image(cfg):
    if "source_url" not in cfg:
        return Image.open(cfg["path"]).convert("RGB")
    request = Request(cfg["source_url"], headers={"User-Agent": "Mozilla/5.0"})
    with urlopen(request, timeout=30) as response:
        return Image.open(response).convert("RGB")


def subject_from_image(img, threshold, mask_filter):
    bg_mask = flood_background_mask(img, threshold)
    subject_mask = ImageOps.invert(bg_mask)
    if mask_filter == "expand":
        subject_mask = subject_mask.filter(ImageFilter.MaxFilter(5)).filter(ImageFilter.GaussianBlur(1.1))
    elif mask_filter == "contract":
        subject_mask = subject_mask.filter(ImageFilter.MinFilter(3)).filter(ImageFilter.GaussianBlur(0.45))
    else:
        subject_mask = subject_mask.filter(ImageFilter.GaussianBlur(0.7))
    bbox = subject_mask.getbbox()
    rgba = img.convert("RGBA")
    rgba.putalpha(subject_mask)
    return rgba.crop(bbox), subject_mask.crop(bbox)


def add_floor_shadow(canvas, subject_mask, box, color, reflection_enabled):
    x, y, w, h = box
    shadow = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(shadow)
    floor_y = y + h - int(h * 0.02)
    draw.ellipse(
        (
            x + int(w * 0.10),
            floor_y - int(h * 0.055),
            x + int(w * 0.92),
            floor_y + int(h * 0.07),
        ),
        fill=(*color, 92),
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(28))
    canvas.alpha_composite(shadow)

    if not reflection_enabled:
        return

    reflection = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    subj_alpha = subject_mask.resize((w, h), Image.Resampling.LANCZOS)
    fade = Image.new("L", (w, h), 0)
    fade_px = fade.load()
    for yy in range(h):
        alpha = max(0, int(44 * (1 - yy / max(1, h * 0.28))))
        for xx in range(w):
            fade_px[xx, yy] = alpha
    refl_alpha = ImageChops.multiply(ImageOps.flip(subj_alpha), fade).filter(ImageFilter.GaussianBlur(1.6))
    refl = Image.new("RGBA", (w, h), (255, 255, 255, 38))
    refl.putalpha(refl_alpha)
    reflection.alpha_composite(refl, (x, floor_y - 4))
    canvas.alpha_composite(reflection)


def enhance(path, cfg):
    img = source_image(cfg)
    if cfg.get("mode") == "full_frame":
        bg = gradient_background(SIZE, cfg["bg_top"], cfg["bg_bottom"], cfg["glow"]).convert("RGB")
        canvas = ImageOps.fit(img, (SIZE, SIZE), method=Image.Resampling.LANCZOS, centering=(0.5, 0.5))
        canvas = ImageEnhance.Contrast(canvas).enhance(cfg["contrast"])
        canvas = ImageEnhance.Color(canvas).enhance(cfg["color"])
        blended = Image.blend(canvas, bg, cfg.get("blend", 0.18))
        shade = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
        draw = ImageDraw.Draw(shade)
        draw.ellipse((110, SIZE - 170, SIZE - 80, SIZE + 10), fill=(*cfg["shadow"], 80))
        shade = shade.filter(ImageFilter.GaussianBlur(34))
        out = Image.alpha_composite(blended.convert("RGBA"), shade)
        if cfg.get("vignette"):
            vignette = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
            vd = ImageDraw.Draw(vignette)
            for r in range(int(SIZE * 0.9), int(SIZE * 0.35), -16):
                alpha = int(68 * (1 - (r - SIZE * 0.35) / (SIZE * 0.55)) ** 1.4)
                vd.rectangle((0, 0, SIZE, SIZE), outline=(80, 48, 57, alpha), width=18)
                vignette = vignette.filter(ImageFilter.GaussianBlur(1))
            out = Image.alpha_composite(out, vignette)
        out = out.convert("RGB")
        out.save(path, "WEBP", quality=94, method=6)
        print(path)
        return

    subject, mask = subject_from_image(img, cfg["threshold"], cfg["mask_filter"])
    subject = ImageEnhance.Contrast(subject).enhance(cfg["contrast"])
    subject = ImageEnhance.Color(subject).enhance(cfg["color"])
    subject = ImageEnhance.Sharpness(subject).enhance(1.08)

    max_side = SIZE - cfg["padding"] * 2
    subject.thumbnail((max_side, max_side), Image.Resampling.LANCZOS)
    mask = mask.resize(subject.size, Image.Resampling.LANCZOS)

    bg = gradient_background(SIZE, cfg["bg_top"], cfg["bg_bottom"], cfg["glow"])
    x = (SIZE - subject.width) // 2
    y = int((SIZE - subject.height) * 0.48)

    add_floor_shadow(bg, mask, (x, y, subject.width, subject.height), cfg["shadow"], cfg["reflection"])
    bg.alpha_composite(subject, (x, y))
    bg.convert("RGB").save(path, "WEBP", quality=94, method=6)
    print(path)


def main():
    for cfg in PRODUCTS:
        enhance(cfg["path"], cfg)


if __name__ == "__main__":
    main()
