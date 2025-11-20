package client;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ImageManager {
    private static ImageManager instance;
    private List<String> imagePaths;
    private Map<Integer, BufferedImage> imageCache;
    private String imageDirectory;
    
    private ImageManager() {
        imagePaths = new ArrayList<>();
        imageCache = new HashMap<>();
        imageDirectory = "resources/images/cards";
        loadImagePaths();
    }
    
    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }
    
    private void loadImagePaths() {
        File dir = new File(imageDirectory);
        
        // Create directory if not exists
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Created images directory: " + imageDirectory);
            System.out.println("Please add card images to: " + dir.getAbsolutePath());
        }
        
        // Load all image files
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".jpg") || 
                       lower.endsWith(".jpeg") || lower.endsWith(".gif");
            });
            
            if (files != null && files.length > 0) {
                for (File file : files) {
                    imagePaths.add(file.getAbsolutePath());
                }
                Collections.sort(imagePaths);
                System.out.println("Loaded " + imagePaths.size() + " card images");
            } else {
                System.out.println("No images found in " + imageDirectory);
                generateDefaultImages();
            }
        }
    }
    
    private void generateDefaultImages() {
        // If no images, use default emoji/icons
        System.out.println("Using default card representations");
    }
    
    public String getImagePath(int index) {
        if (imagePaths.isEmpty()) {
            return null;
        }
        return imagePaths.get(index % imagePaths.size());
    }
    
    public BufferedImage getImage(int index) {
        if (imageCache.containsKey(index)) {
            return imageCache.get(index);
        }
        
        String path = getImagePath(index);
        if (path != null) {
            try {
                BufferedImage img = ImageIO.read(new File(path));
                imageCache.put(index, img);
                return img;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public int getImageCount() {
        return imagePaths.size();
    }
    
    public boolean hasImages() {
        return !imagePaths.isEmpty();
    }
    
    public void clearCache() {
        imageCache.clear();
    }
    
    public void setImageDirectory(String directory) {
        this.imageDirectory = directory;
        imagePaths.clear();
        imageCache.clear();
        loadImagePaths();
    }
    
    /**
     * Get shuffled image indices for game
     */
    public List<Integer> getShuffledIndices(int pairCount) {
        List<Integer> indices = new ArrayList<>();
        
        if (imagePaths.isEmpty()) {
            // Use numbers 0 to pairCount-1
            for (int i = 0; i < pairCount; i++) {
                indices.add(i);
                indices.add(i);
            }
        } else {
            // Use actual images
            int availableImages = Math.min(pairCount, imagePaths.size());
            for (int i = 0; i < availableImages; i++) {
                indices.add(i);
                indices.add(i);
            }
            
            // If need more pairs than available images, repeat
            while (indices.size() < pairCount * 2) {
                int randomIndex = (int)(Math.random() * availableImages);
                indices.add(randomIndex);
                indices.add(randomIndex);
            }
        }
        
        Collections.shuffle(indices);
        return indices;
    }
    
    /**
     * Create default card images programmatically
     */
    public void createDefaultCardSet() {
        try {
            File dir = new File(imageDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // Create simple colored cards as default
            String[] colors = {"red", "blue", "green", "yellow", "purple", 
                             "orange", "pink", "cyan", "magenta", "lime"};
            
            for (int i = 0; i < colors.length; i++) {
                BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2d = img.createGraphics();
                
                // Enable anti-aliasing
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background
                g2d.setColor(getColorByName(colors[i]));
                g2d.fillRoundRect(0, 0, 200, 200, 30, 30);
                
                // Border
                g2d.setColor(java.awt.Color.WHITE);
                g2d.setStroke(new java.awt.BasicStroke(8));
                g2d.drawRoundRect(10, 10, 180, 180, 20, 20);
                
                // Number
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 80));
                String text = String.valueOf(i + 1);
                java.awt.FontMetrics fm = g2d.getFontMetrics();
                int x = (200 - fm.stringWidth(text)) / 2;
                int y = (200 + fm.getAscent() - fm.getDescent()) / 2;
                
                // Text shadow
                g2d.setColor(new java.awt.Color(0, 0, 0, 100));
                g2d.drawString(text, x + 3, y + 3);
                
                // Text
                g2d.setColor(java.awt.Color.WHITE);
                g2d.drawString(text, x, y);
                
                g2d.dispose();
                
                // Save image
                File outputFile = new File(dir, "card_" + (i + 1) + ".png");
                ImageIO.write(img, "png", outputFile);
            }
            
            System.out.println("Created " + colors.length + " default card images");
            loadImagePaths();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private java.awt.Color getColorByName(String colorName) {
        switch(colorName.toLowerCase()) {
            case "red": return new java.awt.Color(231, 76, 60);
            case "blue": return new java.awt.Color(52, 152, 219);
            case "green": return new java.awt.Color(46, 204, 113);
            case "yellow": return new java.awt.Color(241, 196, 15);
            case "purple": return new java.awt.Color(155, 89, 182);
            case "orange": return new java.awt.Color(230, 126, 34);
            case "pink": return new java.awt.Color(255, 107, 129);
            case "cyan": return new java.awt.Color(26, 188, 156);
            case "magenta": return new java.awt.Color(232, 67, 147);
            case "lime": return new java.awt.Color(163, 228, 86);
            default: return java.awt.Color.GRAY;
        }
    }
    
    /**
     * Print instructions for adding custom images
     */
    public void printImageInstructions() {
        System.out.println("\n========== CARD IMAGE INSTRUCTIONS ==========");
        System.out.println("To use custom card images:");
        System.out.println("1. Create folder: " + new File(imageDirectory).getAbsolutePath());
        System.out.println("2. Add image files (PNG, JPG, GIF)");
        System.out.println("3. Recommended size: 200x200 to 400x400 pixels");
        System.out.println("4. Name format: card_1.png, card_2.png, etc.");
        System.out.println("5. You need at least 15 images for HARD mode");
        System.out.println("\nCurrent images: " + imagePaths.size());
        System.out.println("=============================================\n");
    }
}