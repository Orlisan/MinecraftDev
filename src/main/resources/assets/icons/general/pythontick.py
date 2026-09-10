from PIL import Image, ImageDraw

def genera_spunta_pixelata():
    # Crea un'immagine 40x40 con canale Alpha (trasparenza totale: 0, 0, 0, 0)
    img = Image.new("RGBA", (40, 40), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Colore verde brillante per la spunta (RGB + Opacità al 100%)
    VERDE_PIXEL = (46, 204, 113, 255)
    
    # Disegniamo la spunta pixelata usando piccoli blocchi da 3x3 pixel
    # Coordinata del braccio sinistro (scende verso il basso)
    draw.rectangle([10, 20, 12, 22], fill=VERDE_PIXEL)
    draw.rectangle([13, 23, 15, 25], fill=VERDE_PIXEL)
    
    # Il punto più basso (la punta del tick)
    draw.rectangle([16, 26, 18, 28], fill=VERDE_PIXEL)
    
    # Il braccio destro (sale verso l'alto)
    draw.rectangle([19, 23, 21, 25], fill=VERDE_PIXEL)
    draw.rectangle([22, 20, 24, 22], fill=VERDE_PIXEL)
    draw.rectangle([25, 17, 27, 19], fill=VERDE_PIXEL)
    draw.rectangle([28, 14, 30, 16], fill=VERDE_PIXEL)
    
    # Salva il file come PNG mantenendo la trasparenza
    img.save("LangTick.png")
    print("Immagine 'LangTick.png' con spunta verde pixelata generata!")

if __name__ == "__main__":
    genera_spunta_pixelata()
