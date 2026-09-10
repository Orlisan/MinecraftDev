import random
from PIL import Image, ImageDraw

# Configurazione dimensioni immagine e griglia
WIDTH = 360
HEIGHT = 160
RECT_W = 60
RECT_H = 40
COLS = 6
ROWS = 4

# Lista di 24 colori RGB ben assortiti e distinti
COLORS = [
    (230, 25, 75),   # Rosso
    (60, 180, 75),   # Verde
    (255, 225, 25),  # Giallo
    (0, 130, 200),   # Blu
    (245, 130, 48),  # Arancione
    (145, 30, 180),  # Viola
    (70, 240, 240),  # Ciano
    (240, 50, 230),  # Magenta
    (210, 245, 60),  # Lime
    (250, 190, 212), # Rosa
    (0, 128, 128),   # Teal
    (220, 190, 255), # Lavanda
    (170, 110, 40),  # Marrone
    (255, 250, 200), # Beige
    (128, 0, 0),     # Mattone
    (170, 255, 195), # Menta
    (128, 128, 0),   # Oliva
    (255, 215, 180), # Albicocca
    (0, 0, 128),     # Navy
    (128, 128, 128), # Grigio
    (255, 99, 71),   # Pomodoro
    (30, 144, 255),  # Dodger Blue
    (50, 205, 50),   # Lime Green
    (218, 165, 32)   # Oro
]

# Mescola i colori in modo casuale ma riproducibile (seed) per alternarli
random.seed(42)
random.shuffle(COLORS)

def generate_flags_image():
    # Crea una nuova immagine vuota RGB
    img = Image.new("RGB", (WIDTH, HEIGHT))
    draw = ImageDraw.Draw(img)
    
    color_idx = 0
    
    # Ciclo per generare la disposizione 6x4 dei rettangoli
    for row in range(ROWS):
        for col in range(COLS):
            # Calcola le coordinate del rettangolo corrente
            x1 = col * RECT_W
            y1 = row * RECT_H
            x2 = x1 + RECT_W
            y2 = y1 + RECT_H
            
            # Disegna il rettangolo pieno
            draw.rectangle([x1, y1, x2, y2], fill=COLORS[color_idx])
            color_idx += 1
            
    # Salva il risultato finale come PNG
    img.save("LanguagesFlags.png")
    print("Immagine 'LanguagesFlags.png' generata con successo!")

if __name__ == "__main__":
    generate_flags_image()
