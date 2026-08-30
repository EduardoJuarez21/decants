from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/thank-u-next.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/car-thank-u-next.webp"

W, H = 1080, 1600
PINK = (238, 135, 166)
PINK_DEEP = (195, 60, 106)
PINK_SOFT = (255, 206, 221)
ROSE_DARK = (62, 6, 26)
WHITE = (255, 248, 251)
BLACK = (8, 5, 7)
MUTED = (232, 198, 210)

FONT_DIR = Path("C:/Windows/Fonts")
GEORGIA = FONT_DIR / "georgia.ttf"
GEORGIA_BOLD = FONT_DIR / "georgiab.ttf"
SEGOE = FONT_DIR / "segoeui.ttf"
SEGOE_BOLD = FONT_DIR / "segoeuib.ttf"


def font(path: Path, size: int):
    return ImageFont.truetype(str(path), size)


def centered(draw, text, y, fnt, fill=WHITE):
    bbox = draw.textbbox((0, 0), text, font=fnt)
    draw.text(((W - (bbox[2] - bbox[0])) / 2, y), text, font=fnt, fill=fill)


def wrap_text(draw, text, fnt, max_width):
    lines = []
    for raw in text.split("\n"):
        words = raw.split()
        line = ""
        for word in words:
            test = f"{line} {word}".strip()
            if draw.textbbox((0, 0), test, font=fnt)[2] <= max_width:
                line = test
            else:
                if line:
                    lines.append(line)
                line = word
        if line:
            lines.append(line)
    return lines


def draw_icon(draw, kind, cx, cy, small=False):
    r = 24 if small else 42
    color = PINK
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=color, width=2 if small else 3)
    if kind == "family":
        draw.ellipse((cx - 22, cy - 22, cx + 2, cy + 2), outline=color, width=3)
        draw.ellipse((cx - 2, cy - 12, cx + 24, cy + 14), outline=color, width=3)
        draw.line((cx - 4, cy + 12, cx + 22, cy + 30), fill=color, width=3)
    elif kind == "fruit":
        draw.ellipse((cx - 24, cy - 8, cx + 10, cy + 27), outline=color, width=3)
        draw.ellipse((cx - 4, cy - 4, cx + 24, cy + 24), outline=color, width=3)
        draw.arc((cx - 3, cy - 33, cx + 28, cy - 5), 205, 340, fill=color, width=3)
        draw.line((cx - 3, cy - 10, cx + 5, cy - 29), fill=color, width=3)
    elif kind == "coconut":
        draw.ellipse((cx - 25, cy - 18, cx + 26, cy + 27), outline=color, width=3)
        draw.arc((cx - 14, cy - 12, cx + 15, cy + 20), 230, 130, fill=color, width=2)
        for dx in [-8, 0, 8]:
            draw.ellipse((cx + dx - 3, cy - 4, cx + dx + 3, cy + 2), fill=color)
    elif kind == "sweet":
        draw.rounded_rectangle((cx - 27, cy - 15, cx + 27, cy + 15), radius=8, outline=color, width=3)
        draw.line((cx - 10, cy - 15, cx - 2, cy + 15), fill=color, width=2)
        draw.line((cx + 4, cy - 15, cx + 12, cy + 15), fill=color, width=2)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=color, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=color, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=color, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=color, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=color, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=color, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=PINK)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 36
    draw.line((245, yy + 10, 965, yy + 10), fill=PINK_DEEP, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(720, 0, -12):
        alpha = int(88 * (1 - r / 720) ** 1.55)
        gd.ellipse((W // 2 - r, 70 - r, W // 2 + r, 70 + r), fill=(PINK_DEEP[0], PINK_DEEP[1], PINK_DEEP[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 86)
    brand_font = font(GEORGIA, 42)
    h_font = font(SEGOE_BOLD, 33)
    b_font = font(SEGOE, 30)
    small_font = font(SEGOE, 29)

    centered(draw, "THANK U NEXT", 54, title_font, WHITE)
    centered(draw, "ARIANA GRANDE", 158, brand_font, PINK_SOFT)
    draw.line((260, 222, 820, 222), fill=MUTED, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((500, 500), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (500, 500), (230, 132, 164))
    x = (500 - product.width) // 2
    y = (500 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (290, 250), mask)
    draw = ImageDraw.Draw(img)

    y = 790
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Floral Frutal Gourmand Femenina", h_font, b_font)
    y = line_block(draw, y, "fruit", "NOTAS DE SALIDA", "Pera Blanca, Frambuesa", h_font, b_font)
    y = line_block(draw, y, "coconut", "NOTAS DE CORAZON", "Crema de Coco, Petalos de Rosa Rosa", h_font, b_font)
    y = line_block(draw, y, "sweet", "NOTAS DE FONDO", "Azucar de Macaron, Almizcle Aterciopelado", h_font, b_font)

    panel = (88, 1268, 992, 1562)
    draw.rounded_rectangle(panel, radius=18, outline=PINK, width=2)
    draw.line((505, 1300, 505, 1532), fill=PINK_DEEP, width=2)

    rows = [
        ("bars", "INTENSIDAD", "3 / 5"),
        ("clock", "DURACION", "4 - 6 HORAS"),
        ("star", "OCASION", "Dia a dia, Citas, Casual"),
        ("season", "ESTACION", "Primavera / Verano"),
    ]
    yy = 1304
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=PINK)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1540:
            draw.line((235, yy - 14, 940, yy - 14), fill=ROSE_DARK, width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
