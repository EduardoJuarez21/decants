from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/arabe/rebel-9pm.webp"
OUT = ROOT / "src/main/resources/static/img/arabe/car-rebel-9pm.webp"

W, H = 1080, 1600
RED = (188, 30, 55)
RED_SOFT = (232, 79, 103)
RED_DARK = (92, 16, 28)
GREY = (104, 98, 96)
WHITE = (248, 244, 242)
BLACK = (7, 7, 7)

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
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=RED_SOFT, width=2 if small else 3)
    if kind == "family":
        draw.arc((cx - 26, cy - 12, cx + 26, cy + 30), 185, 350, fill=RED_SOFT, width=3)
        draw.line((cx - 18, cy + 11, cx + 18, cy + 11), fill=RED_SOFT, width=3)
        draw.line((cx - 8, cy + 11, cx - 2, cy + 29), fill=RED_SOFT, width=3)
        draw.line((cx + 8, cy + 11, cx + 2, cy + 29), fill=RED_SOFT, width=3)
    elif kind == "fruit":
        draw.polygon([(cx - 4, cy - 29), (cx + 24, cy - 3), (cx + 14, cy + 26), (cx - 22, cy + 18), (cx - 27, cy - 10)], outline=RED_SOFT)
        for dx, dy in [(-11, -6), (1, 4), (11, -8), (-2, 17)]:
            draw.ellipse((cx + dx - 3, cy + dy - 3, cx + dx + 3, cy + dy + 3), fill=RED_SOFT)
    elif kind == "wood":
        draw.rectangle((cx - 22, cy - 24, cx + 22, cy + 24), outline=RED_SOFT, width=3)
        draw.arc((cx - 16, cy - 17, cx + 17, cy + 17), 70, 275, fill=RED_SOFT, width=2)
        draw.line((cx - 4, cy - 23, cx + 10, cy + 23), fill=RED_SOFT, width=2)
    elif kind == "caramel":
        draw.rounded_rectangle((cx - 26, cy - 16, cx + 26, cy + 16), radius=7, outline=RED_SOFT, width=3)
        draw.line((cx - 10, cy - 16, cx - 2, cy + 16), fill=RED_SOFT, width=2)
        draw.line((cx + 4, cy - 16, cx + 12, cy + 16), fill=RED_SOFT, width=2)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=RED_SOFT, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=RED_SOFT, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=RED_SOFT, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=RED_SOFT, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=RED_SOFT, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=RED_SOFT, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=RED_SOFT)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 36
    draw.line((245, yy + 10, 965, yy + 10), fill=RED_DARK, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(700, 0, -12):
        alpha = int(82 * (1 - r / 700) ** 1.55)
        gd.ellipse((W // 2 - r, 70 - r, W // 2 + r, 70 + r), fill=(RED[0], RED[1], RED[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 94)
    brand_font = font(GEORGIA, 44)
    h_font = font(SEGOE_BOLD, 33)
    b_font = font(SEGOE, 30)
    small_font = font(SEGOE, 29)

    centered(draw, "9 PM REBEL", 56, title_font, WHITE)
    centered(draw, "AFNAN", 164, brand_font, RED_SOFT)
    draw.line((260, 228, 820, 228), fill=GREY, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((500, 500), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (500, 500), (239, 239, 239))
    x = (500 - product.width) // 2
    y = (500 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (290, 250), mask)
    draw = ImageDraw.Draw(img)

    y = 790
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Aromatico Frutal Unisex", h_font, b_font)
    y = line_block(draw, y, "fruit", "NOTAS DE SALIDA", "Mandarina, Pina, Manzana Granny Smith", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE CORAZON", "Cedro, Musgo de Roble, Vainilla", h_font, b_font)
    y = line_block(draw, y, "caramel", "NOTAS DE FONDO", "Caramelo, Maderas Secas, Ambar Gris, Almizcle", h_font, b_font)

    panel = (88, 1268, 992, 1562)
    draw.rounded_rectangle(panel, radius=18, outline=RED_SOFT, width=2)
    draw.line((505, 1300, 505, 1532), fill=RED_DARK, width=2)

    rows = [
        ("bars", "INTENSIDAD", "4 / 5"),
        ("clock", "DURACION", "7 - 10 HORAS"),
        ("star", "OCASION", "Dia / Noche, Salidas"),
        ("season", "ESTACION", "Primavera / Verano, Otono"),
    ]
    yy = 1304
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=RED_SOFT)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1540:
            draw.line((235, yy - 14, 940, yy - 14), fill=(75, 26, 33), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
