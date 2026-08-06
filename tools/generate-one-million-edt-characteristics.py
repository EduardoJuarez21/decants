from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/hombre/one-million.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/hombre/car-one-million.webp"

W, H = 1080, 1600
GOLD = (219, 165, 56)
GOLD_SOFT = (154, 113, 45)
WHITE = (246, 241, 229)
BLACK = (3, 3, 3)

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


def centered_shadow(draw, text, y, fnt, fill=WHITE):
    bbox = draw.textbbox((0, 0), text, font=fnt)
    x = (W - (bbox[2] - bbox[0])) / 2
    draw.text((x + 3, y + 3), text, font=fnt, fill=(0, 0, 0))
    draw.text((x, y), text, font=fnt, fill=fill)


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
    draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=GOLD, width=2 if small else 3)
    if kind == "family":
        draw.arc((cx - 24, cy - 24, cx + 24, cy + 24), 210, 330, fill=GOLD, width=3)
        draw.line((cx - 22, cy + 16, cx + 22, cy + 16), fill=GOLD, width=3)
    elif kind == "citrus":
        draw.ellipse((cx - 24, cy - 24, cx + 24, cy + 24), outline=GOLD, width=3)
        draw.line((cx, cy - 24, cx, cy + 24), fill=GOLD, width=2)
        draw.line((cx - 24, cy, cx + 24, cy), fill=GOLD, width=2)
        draw.line((cx - 17, cy - 17, cx + 17, cy + 17), fill=GOLD, width=2)
        draw.line((cx + 17, cy - 17, cx - 17, cy + 17), fill=GOLD, width=2)
    elif kind == "heart":
        pts = [(cx, cy + 24), (cx - 28, cy - 3), (cx - 18, cy - 25), (cx, cy - 10), (cx + 18, cy - 25), (cx + 28, cy - 3)]
        draw.line(pts + [pts[0]], fill=GOLD, width=4, joint="curve")
    elif kind == "wood":
        draw.rectangle((cx - 22, cy - 24, cx + 22, cy + 24), outline=GOLD, width=3)
        draw.arc((cx - 16, cy - 17, cx + 17, cy + 17), 70, 275, fill=GOLD, width=2)
    elif kind == "bars":
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
    yy = y + 52
    for line in wrap_text(draw, body, body_font, 720):
        draw.text((245, yy), line, font=body_font, fill=WHITE)
        yy += 38
    draw.line((245, yy + 10, 965, yy + 10), fill=GOLD_SOFT, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)
    draw = ImageDraw.Draw(img)

    for r in range(620, 0, -10):
        alpha = int(88 * (1 - r / 620) ** 1.6)
        overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        od = ImageDraw.Draw(overlay)
        od.ellipse((W // 2 - r, 370 - r, W // 2 + r, 370 + r), fill=(222, 157, 37, alpha))
        img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 92)
    brand_font = font(GEORGIA, 44)
    sub_font = font(SEGOE, 34)
    h_font = font(SEGOE_BOLD, 34)
    b_font = font(SEGOE, 31)
    small_font = font(SEGOE, 29)

    centered(draw, "1 MILLION", 58, title_font, (38, 25, 5))
    centered(draw, "PACO RABANNE", 162, brand_font, WHITE)
    centered(draw, "EAU DE TOILETTE", 218, sub_font, (38, 25, 5))
    draw.line((260, 276, 820, 276), fill=GOLD, width=2)

    product = Image.open(SOURCE).convert("RGB").resize((460, 460), Image.Resampling.LANCZOS)
    mask = Image.new("L", product.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 460, 460), radius=18, fill=255)
    img.paste(product, (310, 310), mask)
    draw = ImageDraw.Draw(img)

    y = 805
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Amaderado Especiado", h_font, b_font)
    y = line_block(draw, y, "citrus", "NOTAS DE SALIDA", "Mandarina Sanguina, Toronja, Menta", h_font, b_font)
    y = line_block(draw, y, "heart", "NOTAS DE CORAZON", "Canela, Notas Especiadas, Rosa", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Ambar, Cuero, Notas Amaderadas, Pachuli Hindu", h_font, b_font)

    panel = (88, 1288, 992, 1570)
    draw.rounded_rectangle(panel, radius=18, outline=GOLD, width=2)
    draw.line((505, 1322, 505, 1548), fill=GOLD_SOFT, width=2)

    rows = [
        ("bars", "INTENSIDAD", "● ● ● ● ○"),
        ("clock", "DURACION", "8 - 12 HORAS"),
        ("star", "OCASION", "Noche, Fiestas, Citas"),
        ("season", "ESTACION", "Otoño / Invierno, Primavera"),
    ]
    yy = 1327
    for icon, label, value in rows:
        draw_icon(draw, icon, 148, yy + 25, small=True)
        draw.text((235, yy), label, font=small_font, fill=GOLD)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 59
        if yy < 1555:
            draw.line((235, yy - 14, 940, yy - 14), fill=(71, 53, 26), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
