from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/arabe/9am-dive.webp"
OUT = ROOT / "src/main/resources/static/img/arabe/car-9am-dive.webp"

W, H = 1080, 1600
BLUE = (30, 188, 224)
BLUE_SOFT = (107, 224, 245)
BLUE_DARK = (5, 72, 118)
NAVY = (6, 21, 56)
WHITE = (244, 252, 255)
BLACK = (4, 8, 13)
SILVER = (178, 205, 218)

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
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=BLUE_SOFT, width=2 if small else 3)
    if kind == "family":
        draw.arc((cx - 28, cy - 8, cx + 28, cy + 20), 180, 360, fill=BLUE_SOFT, width=3)
        draw.arc((cx - 19, cy - 2, cx + 19, cy + 26), 180, 360, fill=BLUE_SOFT, width=3)
        draw.line((cx - 28, cy + 6, cx - 34, cy + 19), fill=BLUE_SOFT, width=3)
        draw.line((cx + 28, cy + 6, cx + 34, cy + 19), fill=BLUE_SOFT, width=3)
    elif kind == "citrus":
        draw.pieslice((cx - 28, cy - 28, cx + 28, cy + 28), 30, 330, outline=BLUE_SOFT, fill=None, width=3)
        for angle in [70, 110, 150, 190, 230, 270]:
            import math
            x = cx + int(23 * math.cos(math.radians(angle)))
            y = cy + int(23 * math.sin(math.radians(angle)))
            draw.line((cx, cy, x, y), fill=BLUE_SOFT, width=2)
    elif kind == "apple":
        draw.ellipse((cx - 23, cy - 8, cx + 9, cy + 28), outline=BLUE_SOFT, width=3)
        draw.ellipse((cx - 8, cy - 9, cx + 24, cy + 28), outline=BLUE_SOFT, width=3)
        draw.arc((cx, cy - 38, cx + 28, cy - 9), 210, 340, fill=BLUE_SOFT, width=3)
        draw.line((cx, cy - 8, cx + 5, cy - 27), fill=BLUE_SOFT, width=3)
    elif kind == "spice":
        draw.polygon([(cx, cy - 28), (cx + 24, cy - 5), (cx + 15, cy + 25), (cx - 17, cy + 22), (cx - 26, cy - 6)], outline=BLUE_SOFT)
        for dx, dy in [(-8, -5), (6, 0), (0, 12)]:
            draw.ellipse((cx + dx - 4, cy + dy - 4, cx + dx + 4, cy + dy + 4), fill=BLUE_SOFT)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=BLUE_SOFT, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=BLUE_SOFT, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=BLUE_SOFT, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=BLUE_SOFT, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=BLUE_SOFT, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=BLUE_SOFT, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=BLUE_SOFT)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 36
    draw.line((245, yy + 10, 965, yy + 10), fill=BLUE_DARK, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(730, 0, -12):
        alpha = int(90 * (1 - r / 730) ** 1.55)
        gd.ellipse((W // 2 - r, 75 - r, W // 2 + r, 75 + r), fill=(BLUE[0], BLUE[1], BLUE[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 96)
    brand_font = font(GEORGIA, 44)
    h_font = font(SEGOE_BOLD, 33)
    b_font = font(SEGOE, 30)
    small_font = font(SEGOE, 29)

    centered(draw, "9 AM DIVE", 56, title_font, WHITE)
    centered(draw, "AFNAN", 164, brand_font, BLUE_SOFT)
    draw.line((260, 228, 820, 228), fill=SILVER, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((500, 500), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (500, 500), (18, 151, 191))
    x = (500 - product.width) // 2
    y = (500 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (290, 250), mask)
    draw = ImageDraw.Draw(img)

    y = 790
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Aromatica Acuatica Unisex", h_font, b_font)
    y = line_block(draw, y, "citrus", "NOTAS DE SALIDA", "Limon, Menta, Grosella Negra, Pimienta Rosa", h_font, b_font)
    y = line_block(draw, y, "apple", "NOTAS DE CORAZON", "Manzana, Cedro, Incienso", h_font, b_font)
    y = line_block(draw, y, "spice", "NOTAS DE FONDO", "Jengibre, Sandalo, Pachuli, Jazmin", h_font, b_font)

    panel = (88, 1268, 992, 1562)
    draw.rounded_rectangle(panel, radius=18, outline=BLUE_SOFT, width=2)
    draw.line((505, 1300, 505, 1532), fill=BLUE_DARK, width=2)

    rows = [
        ("bars", "INTENSIDAD", "3 / 5"),
        ("clock", "DURACION", "6 - 8 HORAS"),
        ("star", "OCASION", "Dia a dia, Oficina, Calor"),
        ("season", "ESTACION", "Primavera / Verano"),
    ]
    yy = 1304
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=BLUE_SOFT)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1540:
            draw.line((235, yy - 14, 940, yy - 14), fill=(20, 68, 91), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
