from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/arabe/odyssey-mandarin-sky.webp"
OUT = ROOT / "src/main/resources/static/img/arabe/car-odyssey-mandarin-sky.webp"

W, H = 1080, 1600
ORANGE = (245, 121, 20)
ORANGE_SOFT = (255, 158, 55)
AQUA = (38, 203, 226)
AQUA_SOFT = (104, 230, 244)
TEAL_DARK = (5, 69, 85)
WHITE = (246, 253, 255)
BLACK = (4, 10, 13)

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
    color = AQUA_SOFT
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=color, width=2 if small else 3)
    if kind == "family":
        draw.arc((cx - 29, cy - 7, cx + 29, cy + 20), 180, 360, fill=color, width=3)
        draw.arc((cx - 19, cy - 1, cx + 19, cy + 25), 180, 360, fill=color, width=3)
        draw.line((cx - 28, cy + 6, cx - 34, cy + 19), fill=color, width=3)
        draw.line((cx + 28, cy + 6, cx + 34, cy + 19), fill=color, width=3)
    elif kind == "citrus":
        draw.pieslice((cx - 28, cy - 28, cx + 28, cy + 28), 28, 332, outline=color, fill=None, width=3)
        for angle in [70, 110, 150, 190, 230, 270]:
            import math
            x = cx + int(23 * math.cos(math.radians(angle)))
            y = cy + int(23 * math.sin(math.radians(angle)))
            draw.line((cx, cy, x, y), fill=color, width=2)
    elif kind == "caramel":
        draw.rounded_rectangle((cx - 26, cy - 16, cx + 26, cy + 16), radius=7, outline=color, width=3)
        draw.line((cx - 10, cy - 16, cx - 2, cy + 16), fill=color, width=2)
        draw.line((cx + 4, cy - 16, cx + 12, cy + 16), fill=color, width=2)
    elif kind == "wood":
        draw.rectangle((cx - 22, cy - 24, cx + 22, cy + 24), outline=color, width=3)
        draw.arc((cx - 16, cy - 17, cx + 17, cy + 17), 70, 275, fill=color, width=2)
        draw.line((cx - 4, cy - 23, cx + 10, cy + 23), fill=color, width=2)
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
    draw.text((245, y), title, font=title_font, fill=ORANGE_SOFT)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 36
    draw.line((245, yy + 10, 965, yy + 10), fill=TEAL_DARK, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(780, 0, -12):
        alpha = int(86 * (1 - r / 780) ** 1.55)
        gd.ellipse((W // 2 - r, 90 - r, W // 2 + r, 90 + r), fill=(ORANGE[0], ORANGE[1], ORANGE[2], alpha))
    for r in range(650, 0, -12):
        alpha = int(54 * (1 - r / 650) ** 1.6)
        gd.ellipse((W // 2 - r, 430 - r, W // 2 + r, 430 + r), fill=(AQUA[0], AQUA[1], AQUA[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 86)
    brand_font = font(GEORGIA, 42)
    h_font = font(SEGOE_BOLD, 33)
    b_font = font(SEGOE, 30)
    small_font = font(SEGOE, 29)

    centered(draw, "MANDARIN SKY", 54, title_font, WHITE)
    centered(draw, "ARMAF ODYSSEY", 158, brand_font, AQUA_SOFT)
    draw.line((260, 222, 820, 222), fill=ORANGE_SOFT, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((500, 500), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (500, 500), (20, 166, 190))
    x = (500 - product.width) // 2
    y = (500 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (290, 250), mask)
    draw = ImageDraw.Draw(img)

    y = 790
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Ambar Amaderada Masculina", h_font, b_font)
    y = line_block(draw, y, "citrus", "NOTAS DE SALIDA", "Mandarina, Naranja, Azafran, Salvia", h_font, b_font)
    y = line_block(draw, y, "caramel", "NOTAS DE CORAZON", "Caramelo, Haba Tonka, Tagetes", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Ambroxan, Cedro, Vetiver", h_font, b_font)

    panel = (88, 1268, 992, 1562)
    draw.rounded_rectangle(panel, radius=18, outline=AQUA_SOFT, width=2)
    draw.line((505, 1300, 505, 1532), fill=TEAL_DARK, width=2)

    rows = [
        ("bars", "INTENSIDAD", "3 / 5"),
        ("clock", "DURACION", "6 - 8 HORAS"),
        ("star", "OCASION", "Dia, Casual, Citas"),
        ("season", "ESTACION", "Primavera / Verano"),
    ]
    yy = 1304
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=ORANGE_SOFT)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1540:
            draw.line((235, yy - 14, 940, yy - 14), fill=(13, 75, 86), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
