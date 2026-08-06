from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/hombre/one-million-parfum.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/hombre/car-one-million-parfum.webp"

W, H = 1080, 1600
GOLD = (219, 165, 56)
GOLD_SOFT = (154, 113, 45)
WHITE = (246, 241, 229)
MUTED = (205, 195, 175)
BLACK = (3, 3, 3)

FONT_DIR = Path("C:/Windows/Fonts")
GEORGIA = FONT_DIR / "georgia.ttf"
GEORGIA_BOLD = FONT_DIR / "georgiab.ttf"
SEGOE = FONT_DIR / "segoeui.ttf"
SEGOE_BOLD = FONT_DIR / "segoeuib.ttf"


def font(path: Path, size: int) -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(str(path), size)


def centered(draw: ImageDraw.ImageDraw, text: str, y: int, fnt, fill=WHITE):
    bbox = draw.textbbox((0, 0), text, font=fnt)
    draw.text(((W - (bbox[2] - bbox[0])) / 2, y), text, font=fnt, fill=fill)


def wrap_text(draw: ImageDraw.ImageDraw, text: str, fnt, max_width: int):
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


def draw_icon(draw: ImageDraw.ImageDraw, kind: str, cx: int, cy: int):
    draw.ellipse((cx - 42, cy - 42, cx + 42, cy + 42), outline=GOLD, width=3)
    if kind == "family":
        draw.arc((cx - 24, cy - 24, cx + 24, cy + 24), 210, 330, fill=GOLD, width=4)
        draw.line((cx - 22, cy + 16, cx + 22, cy + 16), fill=GOLD, width=3)
        draw.line((cx - 10, cy + 16, cx - 4, cy + 30), fill=GOLD, width=3)
        draw.line((cx + 10, cy + 16, cx + 4, cy + 30), fill=GOLD, width=3)
    elif kind == "sun":
        draw.ellipse((cx - 15, cy - 15, cx + 15, cy + 15), outline=GOLD, width=3)
        for dx, dy in [(0, -30), (0, 30), (-30, 0), (30, 0), (-21, -21), (21, 21), (-21, 21), (21, -21)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=GOLD, width=3)
    elif kind == "heart":
        pts = [(cx, cy + 24), (cx - 28, cy - 3), (cx - 18, cy - 25), (cx, cy - 10), (cx + 18, cy - 25), (cx + 28, cy - 3)]
        draw.line(pts + [pts[0]], fill=GOLD, width=4, joint="curve")
    elif kind == "wood":
        draw.rectangle((cx - 20, cy - 25, cx + 20, cy + 25), outline=GOLD, width=3)
        draw.arc((cx - 17, cy - 20, cx + 17, cy + 20), 80, 280, fill=GOLD, width=2)
        draw.line((cx - 4, cy - 24, cx + 8, cy + 24), fill=GOLD, width=2)
    elif kind == "bars":
        for i, h in enumerate([18, 30, 43, 57]):
            x = cx - 27 + i * 18
            draw.rectangle((x, cy + 28 - h, x + 10, cy + 28), outline=GOLD, width=3)
    elif kind == "clock":
        draw.ellipse((cx - 23, cy - 23, cx + 23, cy + 23), outline=GOLD, width=3)
        draw.line((cx, cy, cx, cy - 15), fill=GOLD, width=4)
        draw.line((cx, cy, cx + 13, cy + 9), fill=GOLD, width=4)
    elif kind == "star":
        pts = [(cx, cy - 31), (cx + 9, cy - 9), (cx + 31, cy - 8), (cx + 13, cy + 6), (cx + 19, cy + 29), (cx, cy + 16), (cx - 19, cy + 29), (cx - 13, cy + 6), (cx - 31, cy - 8), (cx - 9, cy - 9)]
        draw.polygon(pts, outline=GOLD, fill=None)
        draw.line(pts + [pts[0]], fill=GOLD, width=3)
    elif kind == "season":
        draw.ellipse((cx - 16, cy - 16, cx + 16, cy + 16), outline=GOLD, width=3)
        draw.arc((cx - 35, cy - 35, cx + 35, cy + 35), 30, 140, fill=GOLD, width=3)
        draw.arc((cx - 35, cy - 35, cx + 35, cy + 35), 210, 320, fill=GOLD, width=3)


def draw_small_icon(draw: ImageDraw.ImageDraw, kind: str, cx: int, cy: int):
    draw.ellipse((cx - 24, cy - 24, cx + 24, cy + 24), outline=GOLD, width=2)
    if kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=GOLD, width=2)
    elif kind == "clock":
        draw.line((cx, cy, cx, cy - 11), fill=GOLD, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=GOLD, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18), (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=GOLD, width=2)
    elif kind == "season":
        draw.ellipse((cx - 9, cy - 9, cx + 9, cy + 9), outline=GOLD, width=2)
        for dx, dy in [(0, -17), (0, 17), (-17, 0), (17, 0), (-12, -12), (12, 12), (-12, 12), (12, -12)]:
            draw.line((cx + dx * 0.55, cy + dy * 0.55, cx + dx, cy + dy), fill=GOLD, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 150, y + 42)
    draw.text((245, y), title, font=title_font, fill=GOLD)
    lines = wrap_text(draw, body, body_font, 720)
    yy = y + 52
    for line in lines:
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 38
    draw.line((245, yy + 10, 965, yy + 10), fill=GOLD_SOFT, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)
    draw = ImageDraw.Draw(img)

    # Gold radial glow.
    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(620, 0, -10):
        alpha = int(90 * (1 - r / 620) ** 1.6)
        gd.ellipse((W // 2 - r, 30 - r, W // 2 + r, 30 + r), fill=(222, 157, 37, alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 86)
    brand_font = font(GEORGIA, 46)
    sub_font = font(SEGOE, 32)
    h_font = font(SEGOE_BOLD, 34)
    b_font = font(SEGOE, 31)
    small_font = font(SEGOE, 29)

    centered(draw, "1 MILLION PARFUM", 52, title_font, GOLD)
    centered(draw, "RABANNE", 155, brand_font, WHITE)
    draw.line((260, 224, 820, 224), fill=GOLD, width=2)

    product = Image.open(SOURCE).convert("RGB")
    product = product.resize((460, 460), Image.Resampling.LANCZOS)
    mask = Image.new("L", product.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 460, 460), radius=18, fill=255)
    img.paste(product, (310, 250), mask)

    y = 750
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Floral Amaderado", h_font, b_font)
    y = line_block(draw, y, "sun", "NOTAS DE SALIDA", "Toronja, Aceite de Monoi, Sal Marina", h_font, b_font)
    y = line_block(draw, y, "heart", "NOTAS DE CORAZON", "Nardo, Pino, Cashmeran, Canela, Rosa", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Ládano, Cuero, Notas Solares, Ámbar Amaderado, Vainilla, Haba Tonka", h_font, b_font)

    panel = (88, 1245, 992, 1540)
    draw.rounded_rectangle(panel, radius=18, outline=GOLD, width=2)
    draw.line((505, 1278, 505, 1510), fill=GOLD_SOFT, width=2)

    rows = [
        ("bars", "INTENSIDAD", "● ● ● ● ○"),
        ("clock", "DURACION", "8 - 12 HORAS"),
        ("star", "OCASION", "Noche, Eventos, Citas"),
        ("season", "ESTACION", "Primavera / Verano, Otoño"),
    ]
    yy = 1282
    for icon, label, value in rows:
        draw_small_icon(draw, icon, 148, yy + 25)
        draw.text((235, yy), label, font=small_font, fill=GOLD)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1515:
            draw.line((235, yy - 14, 940, yy - 14), fill=(71, 53, 26), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
