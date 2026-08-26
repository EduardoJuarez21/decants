from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "src/main/resources/static/img/arabe/car-club-de-nuit-ombre-oud-intense.webp"

W, H = 1024, 1560
BLACK = (5, 5, 5)
GOLD = (231, 184, 87)
GOLD_SOFT = (245, 214, 150)
GOLD_DARK = (92, 70, 28)
WHITE = (240, 235, 225)

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
    r = 22 if small else 38
    if kind == "family":
        draw.rounded_rectangle((cx - 20, cy - 30, cx + 20, cy + 30), radius=6, outline=GOLD, width=3)
        draw.rectangle((cx - 8, cy - 38, cx + 8, cy - 28), outline=GOLD, width=3)
    elif kind == "top":
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=GOLD, width=3)
        draw.line((cx - 14, cy, cx + 14, cy), fill=GOLD, width=2)
        draw.line((cx, cy - 14, cx, cy + 14), fill=GOLD, width=2)
    elif kind == "heart":
        draw.polygon([(cx, cy - 20), (cx + 26, cy + 6), (cx, cy + 26), (cx - 26, cy + 6)], outline=GOLD)
    elif kind == "base":
        draw.polygon([(cx, cy - 26), (cx + 26, cy), (cx, cy + 26), (cx - 26, cy)], outline=GOLD, width=3)
    elif kind == "bars":
        for i, h in enumerate([10, 16, 23, 30]):
            x = cx - 15 + i * 10
            draw.rectangle((x, cy + 14 - h, x + 5, cy + 14), outline=GOLD, width=2)
    elif kind == "clock":
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=GOLD, width=3)
        draw.line((cx, cy, cx, cy - 12), fill=GOLD, width=3)
        draw.line((cx, cy, cx + 10, cy + 6), fill=GOLD, width=3)
    elif kind == "star":
        pts = [(cx, cy - 18), (cx + 6, cy - 5), (cx + 19, cy - 4), (cx + 8, cy + 4), (cx + 12, cy + 18),
               (cx, cy + 10), (cx - 12, cy + 18), (cx - 8, cy + 4), (cx - 19, cy - 4), (cx - 6, cy - 5)]
        draw.line(pts + [pts[0]], fill=GOLD, width=2)
    elif kind == "season":
        draw.ellipse((cx - r, cy - r, cx + r, cy + r), outline=GOLD, width=3)
        draw.line((cx - r, cy, cx + r, cy), fill=GOLD, width=2)


def line_block(draw, y, icon, title, body, title_font, body_font):
    draw_icon(draw, icon, 130, y + 30)
    draw.text((210, y), title, font=title_font, fill=GOLD)
    yy = y + 48
    for line in wrap_text(draw, body, body_font, 760):
        draw.text((210, yy), line, font=body_font, fill=WHITE)
        yy += 36
    draw.line((90, yy + 12, 934, yy + 12), fill=GOLD_DARK, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)

    glow = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for r in range(560, 0, -10):
        alpha = int(60 * (1 - r / 560) ** 1.6)
        gd.ellipse((W // 2 - r, 260 - r, W // 2 + r, 260 + r), fill=(GOLD[0], GOLD[1], GOLD[2], alpha))
    for r in range(480, 0, -10):
        alpha = int(45 * (1 - r / 480) ** 1.6)
        gd.ellipse((W // 2 - r, H - 260 - r, W // 2 + r, H - 260 + r), fill=(GOLD[0], GOLD[1], GOLD[2], alpha))
    img = Image.alpha_composite(img.convert("RGBA"), glow).convert("RGB")
    draw = ImageDraw.Draw(img)

    draw.rectangle((22, 22, W - 22, H - 22), outline=GOLD, width=2)
    draw.rectangle((30, 30, W - 30, H - 30), outline=GOLD, width=1)

    title_font = font(GEORGIA_BOLD, 74)
    brand_font = font(GEORGIA, 34)
    h_font = font(SEGOE_BOLD, 30)
    b_font = font(SEGOE, 27)
    small_font = font(SEGOE, 26)

    centered(draw, "OMBRE OUD INTENSE", 68, title_font, GOLD_SOFT)
    centered(draw, "ARMAF", 158, brand_font, GOLD)
    draw.line((280, 210, 744, 210), fill=GOLD, width=1)
    draw.ellipse((502, 204, 522, 224), outline=GOLD, width=2)

    y = 260
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Oriental Amaderado", h_font, b_font)
    y = line_block(draw, y, "top", "NOTAS DE SALIDA", "Hojas de Cedro, Naranja, Bergamota", h_font, b_font)
    y = line_block(draw, y, "heart", "NOTAS DE CORAZÓN", "Maracuyá, Tomillo, Sándalo", h_font, b_font)
    y = line_block(draw, y, "base", "NOTAS DE FONDO", "Ambrocenida, Almizcle, Madera de Cedro, Ámbar, Vainilla", h_font, b_font)

    panel = (90, y + 10, 934, y + 305)
    draw.rounded_rectangle(panel, radius=16, outline=GOLD, width=2)
    draw.line((460, panel[1] + 30, 460, panel[3] - 20), fill=GOLD_DARK, width=1)

    rows = [
        ("bars", "INTENSIDAD", "4 / 5"),
        ("clock", "DURACIÓN", "8 - 12 HORAS"),
        ("star", "OCASIÓN", "Noche, Eventos formales"),
        ("season", "ESTACIÓN", "Otoño / Invierno"),
    ]
    yy = panel[1] + 30
    for icon, label, value in rows:
        draw_icon(draw, icon, 135, yy + 22, small=True)
        draw.text((210, yy), label, font=small_font, fill=GOLD)
        draw.text((500, yy), value, font=small_font, fill=WHITE)
        yy += 62
        if yy < panel[3] - 20:
            draw.line((210, yy - 16, 900, yy - 16), fill=(50, 40, 20), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
