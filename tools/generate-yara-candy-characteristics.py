from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/arabe/yara-candy.webp"
OUT = ROOT / "src/main/resources/static/img/arabe/car-yara-candy.webp"

W, H = 1080, 1600
PINK = (245, 91, 139)
PINK_DARK = (142, 31, 67)
PINK_SOFT = (255, 190, 210)
WHITE = (255, 248, 250)
BLACK = (7, 5, 6)
GOLD = (231, 184, 87)

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
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=PINK, width=2 if small else 3)
    if kind == "family":
        draw.ellipse((cx - 22, cy - 22, cx + 2, cy + 2), outline=PINK, width=3)
        draw.ellipse((cx - 2, cy - 12, cx + 24, cy + 14), outline=PINK, width=3)
        draw.line((cx - 4, cy + 12, cx + 22, cy + 30), fill=PINK, width=3)
    elif kind == "citrus":
        draw.pieslice((cx - 27, cy - 27, cx + 27, cy + 27), 30, 330, outline=PINK, fill=None, width=3)
        for angle in [70, 110, 150, 190, 230, 270]:
            import math
            x = cx + int(23 * math.cos(math.radians(angle)))
            y = cy + int(23 * math.sin(math.radians(angle)))
            draw.line((cx, cy, x, y), fill=PINK, width=2)
    elif kind == "candy":
        draw.rounded_rectangle((cx - 24, cy - 14, cx + 24, cy + 14), radius=8, outline=PINK, width=3)
        draw.polygon([(cx - 24, cy), (cx - 39, cy - 14), (cx - 39, cy + 14)], outline=PINK)
        draw.polygon([(cx + 24, cy), (cx + 39, cy - 14), (cx + 39, cy + 14)], outline=PINK)
    elif kind == "wood":
        draw.rectangle((cx - 22, cy - 24, cx + 22, cy + 24), outline=PINK, width=3)
        draw.arc((cx - 16, cy - 17, cx + 17, cy + 17), 70, 275, fill=PINK, width=2)
        draw.line((cx - 4, cy - 23, cx + 10, cy + 23), fill=PINK, width=2)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=PINK, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=PINK, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=PINK, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=PINK, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=PINK, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=PINK, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=PINK)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 38
    draw.line((245, yy + 10, 965, yy + 10), fill=PINK_DARK, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(680, 0, -12):
        alpha = int(88 * (1 - r / 680) ** 1.55)
        gd.ellipse((W // 2 - r, 80 - r, W // 2 + r, 80 + r), fill=(PINK[0], PINK[1], PINK[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 92)
    brand_font = font(GEORGIA, 44)
    h_font = font(SEGOE_BOLD, 34)
    b_font = font(SEGOE, 31)
    small_font = font(SEGOE, 29)

    centered(draw, "YARA CANDY", 54, title_font, PINK_SOFT)
    centered(draw, "LATTAFA", 160, brand_font, WHITE)
    draw.line((260, 226, 820, 226), fill=GOLD, width=2)

    product = Image.open(SOURCE).convert("RGB").resize((460, 460), Image.Resampling.LANCZOS)
    mask = Image.new("L", product.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 460, 460), radius=18, fill=255)
    img.paste(product, (310, 250), mask)
    draw = ImageDraw.Draw(img)

    y = 750
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Floral Frutal Gourmand", h_font, b_font)
    y = line_block(draw, y, "citrus", "NOTAS DE SALIDA", "Mandarina Verde, Grosella Negra", h_font, b_font)
    y = line_block(draw, y, "candy", "NOTAS DE CORAZON", "Dulce Efervescente de Fresa, Gardenia", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Vainilla, Almizcle, Ambar, Sandalo", h_font, b_font)

    panel = (88, 1245, 992, 1540)
    draw.rounded_rectangle(panel, radius=18, outline=PINK, width=2)
    draw.line((505, 1278, 505, 1510), fill=PINK_DARK, width=2)

    rows = [
        ("bars", "INTENSIDAD", "4 / 5"),
        ("clock", "DURACION", "6 - 10 HORAS"),
        ("star", "OCASION", "Dia a dia, Citas, Salidas"),
        ("season", "ESTACION", "Primavera / Verano"),
    ]
    yy = 1282
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=PINK)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1515:
            draw.line((235, yy - 14, 940, yy - 14), fill=(92, 35, 57), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
