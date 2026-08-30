from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/hombre/valentino-uomo-born-in-roma-intense.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/hombre/car-valentino-uomo-born-in-roma-intense.webp"

W, H = 1080, 1600
PINK = (238, 112, 170)
PINK_SOFT = (255, 169, 209)
PINK_DEEP = (150, 35, 89)
CHARCOAL = (18, 15, 18)
WHITE = (248, 242, 246)
BLACK = (4, 3, 4)
SILVER = (170, 156, 164)

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
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=PINK_SOFT, width=2 if small else 3)
    if kind == "family":
        draw.arc((cx - 24, cy - 24, cx + 24, cy + 24), 210, 330, fill=PINK_SOFT, width=4)
        draw.line((cx - 22, cy + 16, cx + 22, cy + 16), fill=PINK_SOFT, width=3)
        draw.line((cx - 10, cy + 16, cx - 4, cy + 30), fill=PINK_SOFT, width=3)
        draw.line((cx + 10, cy + 16, cx + 4, cy + 30), fill=PINK_SOFT, width=3)
    elif kind == "vanilla":
        draw.line((cx - 18, cy + 25, cx - 2, cy - 25), fill=PINK_SOFT, width=4)
        draw.line((cx + 5, cy + 25, cx + 19, cy - 25), fill=PINK_SOFT, width=4)
        draw.arc((cx - 15, cy - 23, cx + 12, cy + 8), 255, 80, fill=PINK_SOFT, width=2)
    elif kind == "herbal":
        draw.arc((cx - 12, cy - 31, cx + 28, cy + 10), 120, 300, fill=PINK_SOFT, width=3)
        draw.arc((cx - 28, cy - 3, cx + 12, cy + 31), 300, 120, fill=PINK_SOFT, width=3)
        draw.line((cx - 25, cy + 24, cx + 25, cy - 24), fill=PINK_SOFT, width=3)
    elif kind == "wood":
        draw.rectangle((cx - 22, cy - 24, cx + 22, cy + 24), outline=PINK_SOFT, width=3)
        draw.arc((cx - 16, cy - 17, cx + 17, cy + 17), 70, 275, fill=PINK_SOFT, width=2)
        draw.line((cx - 4, cy - 23, cx + 10, cy + 23), fill=PINK_SOFT, width=2)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=PINK_SOFT, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=PINK_SOFT, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=PINK_SOFT, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=PINK_SOFT, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=PINK_SOFT, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=PINK_SOFT, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=PINK_SOFT)
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
    for r in range(730, 0, -12):
        alpha = int(84 * (1 - r / 730) ** 1.55)
        gd.ellipse((W // 2 - r, 70 - r, W // 2 + r, 70 + r), fill=(PINK[0], PINK[1], PINK[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 62)
    brand_font = font(GEORGIA, 42)
    h_font = font(SEGOE_BOLD, 33)
    b_font = font(SEGOE, 30)
    small_font = font(SEGOE, 29)

    centered(draw, "BORN IN ROMA INTENSE", 58, title_font, WHITE)
    centered(draw, "VALENTINO UOMO", 146, brand_font, PINK_SOFT)
    draw.line((250, 212, 830, 212), fill=SILVER, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product.thumbnail((500, 500), Image.Resampling.LANCZOS)
    product_canvas = Image.new("RGB", (500, 500), CHARCOAL)
    x = (500 - product.width) // 2
    y = (500 - product.height) // 2
    product_canvas.paste(product, (x, y))
    mask = Image.new("L", product_canvas.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 500, 500), radius=18, fill=255)
    img.paste(product_canvas, (290, 238), mask)
    draw = ImageDraw.Draw(img)

    y = 776
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Ambar Fougère Masculina", h_font, b_font)
    y = line_block(draw, y, "vanilla", "NOTAS DE SALIDA", "Vainilla Bourbon", h_font, b_font)
    y = line_block(draw, y, "herbal", "NOTAS DE CORAZON", "Lavanda / Lavandin", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Vetiver Ahumado", h_font, b_font)

    panel = (88, 1268, 992, 1562)
    draw.rounded_rectangle(panel, radius=18, outline=PINK_SOFT, width=2)
    draw.line((505, 1300, 505, 1532), fill=PINK_DEEP, width=2)

    rows = [
        ("bars", "INTENSIDAD", "4 / 5"),
        ("clock", "DURACION", "8 - 10 HORAS"),
        ("star", "OCASION", "Noche, Citas, Eventos"),
        ("season", "ESTACION", "Otono / Invierno"),
    ]
    yy = 1304
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=PINK_SOFT)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1540:
            draw.line((235, yy - 14, 940, yy - 14), fill=(80, 28, 52), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
