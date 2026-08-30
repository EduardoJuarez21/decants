from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/valentino-donna-born-in-roma-green-stravaganza.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/car-valentino-donna-born-in-roma-green-stravaganza.webp"

W, H = 1080, 1600
GREEN = (156, 220, 52)
GREEN_SOFT = (195, 246, 97)
GREEN_DEEP = (62, 118, 21)
EMERALD = (12, 58, 30)
WHITE = (247, 254, 242)
BLACK = (3, 8, 4)
SILVER = (176, 194, 166)

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
    color = GREEN_SOFT
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=color, width=2 if small else 3)
    if kind == "family":
        draw.ellipse((cx - 22, cy - 22, cx + 2, cy + 2), outline=color, width=3)
        draw.ellipse((cx - 2, cy - 12, cx + 24, cy + 14), outline=color, width=3)
        draw.line((cx - 4, cy + 12, cx + 22, cy + 30), fill=color, width=3)
    elif kind == "tea":
        draw.arc((cx - 29, cy - 7, cx + 29, cy + 21), 180, 360, fill=color, width=3)
        draw.rectangle((cx - 24, cy - 4, cx + 24, cy + 18), outline=color, width=3)
        draw.arc((cx + 18, cy - 2, cx + 42, cy + 18), 270, 90, fill=color, width=3)
    elif kind == "floral":
        for dx, dy in [(0, -19), (18, 0), (0, 19), (-18, 0)]:
            draw.ellipse((cx + dx - 12, cy + dy - 12, cx + dx + 12, cy + dy + 12), outline=color, width=3)
        draw.ellipse((cx - 8, cy - 8, cx + 8, cy + 8), fill=color)
    elif kind == "vanilla":
        draw.line((cx - 18, cy + 25, cx - 2, cy - 25), fill=color, width=4)
        draw.line((cx + 5, cy + 25, cx + 19, cy - 25), fill=color, width=4)
        draw.arc((cx - 15, cy - 23, cx + 12, cy + 8), 255, 80, fill=color, width=2)
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
    draw.text((245, y), title, font=title_font, fill=GREEN_SOFT)
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 36
    draw.line((245, yy + 10, 965, yy + 10), fill=GREEN_DEEP, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(760, 0, -12):
        alpha = int(88 * (1 - r / 760) ** 1.55)
        gd.ellipse((W // 2 - r, 80 - r, W // 2 + r, 80 + r), fill=(GREEN[0], GREEN[1], GREEN[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 55)
    brand_font = font(GEORGIA, 40)
    h_font = font(SEGOE_BOLD, 33)
    b_font = font(SEGOE, 30)
    small_font = font(SEGOE, 29)

    centered(draw, "GREEN STRAVAGANZA", 58, title_font, WHITE)
    centered(draw, "VALENTINO DONNA", 136, brand_font, GREEN_SOFT)
    draw.line((250, 202, 830, 202), fill=SILVER, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((500, 500), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (500, 500), EMERALD)
    x = (500 - product.width) // 2
    y = (500 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (290, 232), mask)
    draw = ImageDraw.Draw(img)

    y = 772
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Floral Ambarada Amaderada Femenina", h_font, b_font)
    y = line_block(draw, y, "tea", "NOTAS DE SALIDA", "Te Lapsang Souchong", h_font, b_font)
    y = line_block(draw, y, "floral", "NOTAS DE CORAZON", "Jazmin Absoluto", h_font, b_font)
    y = line_block(draw, y, "vanilla", "NOTAS DE FONDO", "Extracto de Vainilla", h_font, b_font)

    panel = (88, 1268, 992, 1562)
    draw.rounded_rectangle(panel, radius=18, outline=GREEN_SOFT, width=2)
    draw.line((505, 1300, 505, 1532), fill=GREEN_DEEP, width=2)

    rows = [
        ("bars", "INTENSIDAD", "3 / 5"),
        ("clock", "DURACION", "6 - 8 HORAS"),
        ("star", "OCASION", "Dia, Citas, Salidas"),
        ("season", "ESTACION", "Primavera / Verano"),
    ]
    yy = 1304
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=GREEN_SOFT)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1540:
            draw.line((235, yy - 14, 940, yy - 14), fill=(26, 86, 38), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
