from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/sweet-like-candy.webp"
OUT = ROOT / "src/main/resources/static/img/alta-perfumeria/mujer/car-sweet-like-candy.webp"

W, H = 1080, 1600
PINK = (236, 136, 171)
PINK_DEEP = (198, 75, 127)
PINK_SOFT = (250, 205, 221)
WHITE = (255, 248, 250)
MUTED = (238, 217, 224)
BLACK = (8, 5, 8)

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


def draw_icon(draw, kind, cx, cy):
    draw.ellipse((cx - 42, cy - 42, cx + 42, cy + 42), outline=PINK, width=3)
    if kind == "family":
        draw.ellipse((cx - 22, cy - 22, cx + 2, cy + 2), outline=PINK, width=3)
        draw.ellipse((cx - 2, cy - 12, cx + 24, cy + 14), outline=PINK, width=3)
        draw.line((cx - 4, cy + 12, cx + 22, cy + 30), fill=PINK, width=3)
    elif kind == "fruit":
        draw.ellipse((cx - 24, cy - 8, cx + 10, cy + 27), outline=PINK, width=3)
        draw.arc((cx - 3, cy - 33, cx + 28, cy - 5), 205, 340, fill=PINK, width=3)
        draw.line((cx - 3, cy - 10, cx + 5, cy - 29), fill=PINK, width=3)
    elif kind == "cream":
        pts = [(cx - 28, cy + 25), (cx - 16, cy - 4), (cx, cy - 27), (cx + 17, cy - 1), (cx + 29, cy + 25)]
        draw.line(pts, fill=PINK, width=4, joint="curve")
        draw.arc((cx - 27, cy + 6, cx + 28, cy + 33), 180, 360, fill=PINK, width=3)
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


def draw_small_icon(draw, kind, cx, cy):
    draw.ellipse((cx - 24, cy - 24, cx + 24, cy + 24), outline=PINK, width=2)
    if kind == "bars":
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
    draw.line((245, yy + 10, 965, yy + 10), fill=PINK_DEEP, width=1)
    return yy + 34


def main():
    img = Image.new("RGB", (W, H), BLACK)
    draw = ImageDraw.Draw(img)

    # Soft pink glow.
    for r in range(640, 0, -12):
        alpha = int(80 * (1 - r / 640) ** 1.55)
        color = (PINK_DEEP[0], PINK_DEEP[1], PINK_DEEP[2])
        overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        od = ImageDraw.Draw(overlay)
        od.ellipse((W // 2 - r, 40 - r, W // 2 + r, 40 + r), fill=(*color, alpha))
        img = Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB")
    draw = ImageDraw.Draw(img)

    title_font = font(GEORGIA_BOLD, 72)
    brand_font = font(GEORGIA, 42)
    h_font = font(SEGOE_BOLD, 34)
    b_font = font(SEGOE, 31)
    small_font = font(SEGOE, 29)

    centered(draw, "SWEET LIKE CANDY", 58, title_font, PINK_SOFT)
    centered(draw, "ARIANA GRANDE", 152, brand_font, WHITE)
    draw.line((260, 222, 820, 222), fill=PINK, width=2)

    product = Image.open(SOURCE).convert("RGB").resize((460, 460), Image.Resampling.LANCZOS)
    mask = Image.new("L", product.size, 0)
    md = ImageDraw.Draw(mask)
    md.rounded_rectangle((0, 0, 460, 460), radius=18, fill=255)
    img.paste(product, (310, 250), mask)
    draw = ImageDraw.Draw(img)

    y = 750
    y = line_block(draw, y, "family", "FAMILIA OLFATIVA", "Floral Frutal Gourmand", h_font, b_font)
    y = line_block(draw, y, "fruit", "NOTAS DE SALIDA", "Mora, Pera, Bergamota", h_font, b_font)
    y = line_block(draw, y, "cream", "NOTAS DE CORAZON", "Crema Batida, Malvavisco, Grosella Negra, Jazmin, Frangipani, Madreselva", h_font, b_font)
    y = line_block(draw, y, "wood", "NOTAS DE FONDO", "Vainilla, Madera de Cachemira", h_font, b_font)

    panel = (88, 1300, 992, 1585)
    draw.rounded_rectangle(panel, radius=18, outline=PINK, width=2)
    draw.line((505, 1330, 505, 1558), fill=PINK_DEEP, width=2)

    rows = [
        ("bars", "INTENSIDAD", "● ● ● ○ ○"),
        ("clock", "DURACION", "4 - 6 HORAS"),
        ("star", "OCASION", "Dia a dia, Citas, Salidas casuales"),
        ("season", "ESTACION", "Primavera / Verano, Otoño"),
    ]
    yy = 1336
    for icon, label, value in rows:
        draw_small_icon(draw, icon, 148, yy + 25)
        draw.text((235, yy), label, font=small_font, fill=PINK)
        draw.text((545, yy), value, font=small_font, fill=WHITE)
        yy += 61
        if yy < 1515:
            draw.line((235, yy - 14, 940, yy - 14), fill=(92, 38, 61), width=1)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "WEBP", quality=94, method=6)
    print(OUT)


if __name__ == "__main__":
    main()
