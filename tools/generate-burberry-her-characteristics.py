from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/burberry-her.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/car-burberry-her.webp"

W, H = 1080, 1600
BLUSH = (226, 168, 178)
BLUSH_DARK = (143, 80, 91)
BLUSH_SOFT = (255, 222, 229)
WHITE = (255, 250, 250)
BLACK = (8, 7, 8)
GOLD = (214, 174, 91)

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
    width = 2 if small else 3
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=BLUSH, width=width)
    if kind == "family":
        draw.ellipse((cx - 24, cy - 16, cx + 2, cy + 10), outline=BLUSH, width=3)
        draw.ellipse((cx - 2, cy - 8, cx + 25, cy + 19), outline=BLUSH, width=3)
        draw.line((cx - 5, cy + 10, cx + 20, cy + 29), fill=BLUSH, width=3)
    elif kind == "berries":
        for dx, dy, rr in [(-16, -9, 11), (5, -17, 10), (15, 4, 12), (-7, 12, 10)]:
            draw.ellipse((cx + dx - rr, cy + dy - rr, cx + dx + rr, cy + dy + rr), outline=BLUSH, width=3)
        draw.arc((cx - 3, cy - 34, cx + 28, cy - 5), 205, 340, fill=BLUSH, width=3)
    elif kind == "flower":
        for dx, dy in [(0, -21), (21, 0), (0, 21), (-21, 0)]:
            draw.ellipse((cx + dx - 11, cy + dy - 11, cx + dx + 11, cy + dy + 11), outline=BLUSH, width=3)
        draw.ellipse((cx - 8, cy - 8, cx + 8, cy + 8), outline=BLUSH, width=3)
    elif kind == "wood":
        draw.rectangle((cx - 22, cy - 24, cx + 22, cy + 24), outline=BLUSH, width=3)
        draw.arc((cx - 16, cy - 17, cx + 17, cy + 17), 70, 275, fill=BLUSH, width=2)
        draw.line((cx - 4, cy - 23, cx + 10, cy + 23), fill=BLUSH, width=2)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=BLUSH, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=BLUSH, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=BLUSH, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=BLUSH, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=BLUSH, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=BLUSH, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=BLUSH)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 34
    draw.line((245, yy + 10, 965, yy + 10), fill=BLUSH_DARK, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(690, 0, -12):
        alpha = int(82 * (1 - r / 690) ** 1.55)
        gd.ellipse((W // 2 - r, 75 - r, W // 2 + r, 75 + r), fill=(BLUSH[0], BLUSH[1], BLUSH[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 90)
    brand_font = font(GEORGIA, 44)
    h_font = font(SEGOE_BOLD, 32)
    b_font = font(SEGOE, 29)
    small_font = font(SEGOE, 29)

    centered(draw, "BURBERRY HER", 56, title_font, BLUSH_SOFT)
    centered(draw, "BURBERRY", 162, brand_font, WHITE)
    draw.line((260, 226, 820, 226), fill=GOLD, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((430, 430), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (430, 430), WHITE)
    x = (430 - product.width) // 2
    y = (430 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (325, 245), mask)
    draw = ImageDraw.Draw(img)

    y = 720
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Floral Frutal Gourmand", h_font, b_font)
    y = line_block(draw, y, "berries", "NOTAS DE SALIDA", "Fresa, Frambuesa, Zarzamora, Cereza Acida, Grosellas Negras, Mandarina, Limon", h_font, b_font)
    y = line_block(draw, y, "flower", "NOTAS DE CORAZON", "Violeta, Jazmin", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Almizcle, Vainilla, Cachemira, Maderas, Ambar, Musgo de Roble, Pachuli", h_font, b_font)

    panel = (88, 1274, 992, 1568)
    draw.rounded_rectangle(panel, radius=18, outline=BLUSH, width=2)
    draw.line((505, 1306, 505, 1538), fill=BLUSH_DARK, width=2)

    rows = [
        ("bars", "INTENSIDAD", "3 / 5"),
        ("clock", "DURACION", "6 - 8 HORAS"),
        ("star", "OCASION", "Dia a dia, Oficina, Citas"),
        ("season", "ESTACION", "Primavera / Verano, Otono"),
    ]
    yy = 1310
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=BLUSH)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1545:
            draw.line((235, yy - 14, 940, yy - 14), fill=(88, 42, 51), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
