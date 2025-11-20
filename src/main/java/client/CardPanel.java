package client;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CardPanel extends JPanel {
    private int index;
    private int value;
    private boolean flipped;
    private boolean matched;
    private CardClickListener listener;
    private boolean enabled;
    private BufferedImage cardImage;
    private BufferedImage backImage;
    
    // Animation
    private float flipAngle = 0f;
    private Timer flipTimer;
    private boolean isFlipping = false;
    
    // Modern Design Colors
    private static final Color CARD_BORDER = new Color(52, 73, 94);
    private static final Color CARD_SHADOW = new Color(0, 0, 0, 50);
    private static final Color MATCHED_GLOW = new Color(46, 204, 113);
    private static final Color HOVER_COLOR = new Color(52, 152, 219);
    
    // Card back gradient
    private static final Color BACK_COLOR_1 = new Color(52, 152, 219);
    private static final Color BACK_COLOR_2 = new Color(142, 68, 173);
    
    public CardPanel(int index) {
        this.index = index;
        this.flipped = false;
        this.matched = false;
        this.enabled = true;
        this.value = -1;
        
        setPreferredSize(new Dimension(100, 120));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Load default back image
        loadBackImage();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enabled && !flipped && !matched && !isFlipping && listener != null) {
                    listener.onCardClicked(CardPanel.this);
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                if (enabled && !flipped && !matched && !isFlipping) {
                    repaint();
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                repaint();
            }
        });
    }
    
    private void loadBackImage() {
        // Try to load custom back image
        try {
            File backFile = new File("resources/images/card_back.png");
            if (backFile.exists()) {
                backImage = ImageIO.read(backFile);
            }
        } catch (IOException e) {
            backImage = null; // Will use gradient instead
        }
    }
    
    public void setCardImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                cardImage = ImageIO.read(imageFile);
            } else {
                // Try resources folder
                imageFile = new File("resources/images/" + imagePath);
                if (imageFile.exists()) {
                    cardImage = ImageIO.read(imageFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            cardImage = null;
        }
        if (flipped || matched) {
            repaint();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw shadow
        g2d.setColor(CARD_SHADOW);
        g2d.fillRoundRect(4, 4, width - 8, height - 8, 15, 15);
        
        // Main card body
        RoundRectangle2D cardRect = new RoundRectangle2D.Float(2, 2, width - 4, height - 4, 12, 12);
        
        if (matched) {
            // Matched card - glow effect
            g2d.setColor(MATCHED_GLOW);
            g2d.setStroke(new BasicStroke(3));
            g2d.draw(cardRect);
            
            GradientPaint gp = new GradientPaint(0, 0, new Color(46, 204, 113, 200),
                                                 0, height, new Color(39, 174, 96, 200));
            g2d.setPaint(gp);
        } else if (flipped) {
            // Front - white background
            g2d.setColor(Color.WHITE);
        } else {
            // Back - gradient
            Point2D center = new Point2D.Float(width / 2f, height / 2f);
            float radius = Math.max(width, height);
            
            if (getMousePosition() != null && enabled) {
                // Hover effect
                RadialGradientPaint rgp = new RadialGradientPaint(
                    center, radius,
                    new float[]{0f, 0.7f, 1f},
                    new Color[]{HOVER_COLOR.brighter(), HOVER_COLOR, BACK_COLOR_2}
                );
                g2d.setPaint(rgp);
            } else {
                GradientPaint gp = new GradientPaint(0, 0, BACK_COLOR_1, width, height, BACK_COLOR_2);
                g2d.setPaint(gp);
            }
        }
        
        g2d.fill(cardRect);
        
        // Draw border
        g2d.setColor(CARD_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(cardRect);
        
        // Draw content
        if (flipped || matched) {
            drawFront(g2d, width, height);
        } else {
            drawBack(g2d, width, height);
        }
    }
    
    private void drawBack(Graphics2D g2d, int width, int height) {
        if (backImage != null) {
            // Draw custom back image
            int imgWidth = (int)(width * 0.7);
            int imgHeight = (int)(height * 0.7);
            int x = (width - imgWidth) / 2;
            int y = (height - imgHeight) / 2;
            g2d.drawImage(backImage, x, y, imgWidth, imgHeight, null);
        } else {
            // Draw default pattern
            g2d.setColor(new Color(255, 255, 255, 100));
            
            // Question mark with glow
            Font font = new Font("Arial", Font.BOLD, 60);
            g2d.setFont(font);
            String text = "?";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height + fm.getAscent() - fm.getDescent()) / 2;
            
            // Glow effect
            g2d.setColor(new Color(255, 255, 255, 50));
            for (int i = 3; i > 0; i--) {
                g2d.drawString(text, x - i, y - i);
                g2d.drawString(text, x + i, y + i);
            }
            
            // Main text
            g2d.setColor(Color.WHITE);
            g2d.drawString(text, x, y);
            
            // Decorative circles
            g2d.setColor(new Color(255, 255, 255, 80));
            g2d.fillOval(10, 10, 20, 20);
            g2d.fillOval(width - 30, 10, 20, 20);
            g2d.fillOval(10, height - 30, 20, 20);
            g2d.fillOval(width - 30, height - 30, 20, 20);
        }
    }
    
    private void drawFront(Graphics2D g2d, int width, int height) {
        if (cardImage != null) {
            // Draw card image with padding
            int padding = 8;
            int imgWidth = width - (padding * 2);
            int imgHeight = height - (padding * 2);

            // Calculate aspect ratio to maintain proportions
            float imgAspect = (float) cardImage.getWidth() / cardImage.getHeight();
            float panelAspect = (float) imgWidth / imgHeight;

            int drawWidth, drawHeight, drawX, drawY;

            if (imgAspect > panelAspect) {
                drawWidth = imgWidth;
                drawHeight = (int) (imgWidth / imgAspect);
                drawX = padding;
                drawY = padding + (imgHeight - drawHeight) / 2;
            } else {
                drawHeight = imgHeight;
                drawWidth = (int) (imgHeight * imgAspect);
                drawY = padding;
                drawX = padding + (imgWidth - drawWidth) / 2;
            }

            // Add subtle shadow to image
            g2d.setColor(new Color(0, 0, 0, 30));
            g2d.fillRect(drawX + 2, drawY + 2, drawWidth, drawHeight);

            // Draw image - KHÔNG VẼ SỐ NỮA
            g2d.drawImage(cardImage, drawX, drawY, drawWidth, drawHeight, null);

        } else {
            // Fallback: Draw value number với nice design (khi không có hình)
            g2d.setColor(new Color(52, 73, 94));
            Font font = new Font("Arial", Font.BOLD, 48);
            g2d.setFont(font);
            String text = String.valueOf(value);
            FontMetrics fm = g2d.getFontMetrics();
            int x = (width - fm.stringWidth(text)) / 2;
            int y = (height + fm.getAscent() - fm.getDescent()) / 2;

            // Shadow
            g2d.setColor(new Color(0, 0, 0, 100));
            g2d.drawString(text, x + 2, y + 2);

            // Main text with gradient
            GradientPaint gp = new GradientPaint(x, y - 20, new Color(52, 152, 219),
                                                x, y + 20, new Color(142, 68, 173));
            g2d.setPaint(gp);
            g2d.drawString(text, x, y);
        }
    }
    
    private void drawValueBadge(Graphics2D g2d, int width, int height) {
        if (value < 0) return;
        
        // Small badge in corner
        int badgeSize = 24;
        int badgeX = width - badgeSize - 5;
        int badgeY = 5;
        
        // Badge background
        g2d.setColor(new Color(52, 152, 219, 200));
        g2d.fillOval(badgeX, badgeY, badgeSize, badgeSize);
        
        // Badge border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(badgeX, badgeY, badgeSize, badgeSize);
        
        // Badge text
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        String text = String.valueOf(value);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = badgeX + (badgeSize - fm.stringWidth(text)) / 2;
        int textY = badgeY + (badgeSize + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);
    }
    
    public void flip() {
        if (isFlipping) return;
        
        isFlipping = true;
        flipAngle = 0f;
        
        flipTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flipAngle += 10f;
                if (flipAngle >= 90f) {
                    flipped = true;
                    flipAngle = 0f;
                    flipTimer.stop();
                    isFlipping = false;
                }
                repaint();
            }
        });
        flipTimer.start();
    }
    
    public void unflip() {
        if (isFlipping) return;
        
        isFlipping = true;
        flipAngle = 0f;
        
        flipTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flipAngle += 10f;
                if (flipAngle >= 90f) {
                    flipped = false;
                    flipAngle = 0f;
                    flipTimer.stop();
                    isFlipping = false;
                }
                repaint();
            }
        });
        flipTimer.start();
    }
    
    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            this.flipped = true;
            // Pulse animation
            Timer pulseTimer = new Timer(50, null);
            final int[] scale = {100};
            pulseTimer.addActionListener(new ActionListener() {
                int count = 0;
                @Override
                public void actionPerformed(ActionEvent e) {
                    count++;
                    scale[0] = 100 + (int)(Math.sin(count * 0.3) * 10);
                    repaint();
                    if (count > 20) {
                        pulseTimer.stop();
                    }
                }
            });
            pulseTimer.start();
        }
        repaint();
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getValue() {
        return value;
    }
    
    public boolean isFlipped() {
        return flipped;
    }
    
    public boolean isMatched() {
        return matched;
    }
    
    public void setCardEnabled(boolean enabled) {
        this.enabled = enabled;
        setCursor(enabled ? new Cursor(Cursor.HAND_CURSOR) : 
                           new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public void setCardClickListener(CardClickListener listener) {
        this.listener = listener;
    }
    
    public void reset() {
        flipped = false;
        matched = false;
        value = -1;
        enabled = true;
        cardImage = null;
        flipAngle = 0f;
        if (flipTimer != null) {
            flipTimer.stop();
        }
        isFlipping = false;
        repaint();
    }
    
    public interface CardClickListener {
        void onCardClicked(CardPanel card);
    }
}