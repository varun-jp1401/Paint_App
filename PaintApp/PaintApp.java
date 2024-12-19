import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.awt.image.BufferedImage;

public class PaintApp extends JFrame {

    private int prevX, prevY, currentX, currentY, startX, startY;
    private boolean dragging = false;
    private String selectedTool = "Pencil";
    private Color selectedColor = Color.BLACK;
    private String selectedShape = "Freehand";
    private boolean eraserActive = false;
    private int brushWidth = 2;
    private int eraserWidth = 10;
    private int sprayDensity = 20;
    private boolean isDrawingShape = false;

    private DrawingPanel drawingPanel;

    public PaintApp() {
        setTitle("Enhanced Paint Application");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);

        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
        toolbarPanel.setPreferredSize(new Dimension(150, getHeight()));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Add padding
        add(toolbarPanel, BorderLayout.WEST);

        JLabel toolbarTitle = new JLabel("Tools", SwingConstants.CENTER);
        toolbarTitle.setFont(new Font("Arial", Font.BOLD, 16));
        toolbarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(toolbarTitle);

        toolbarPanel.add(Box.createVerticalStrut(10));  // Add space between items

        JLabel brushLabel = new JLabel("Brush", SwingConstants.CENTER);
        brushLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(brushLabel);

        String[] tools = {"Pencil", "Spray", "Brush", "Marker", "Highlighter"};
        JComboBox<String> toolBox = new JComboBox<>(tools);
        toolBox.setMaximumSize(new Dimension(120, 25));
        toolBox.setSelectedItem("Pencil");
        toolBox.addActionListener(e -> {
            selectedTool = (String) toolBox.getSelectedItem();
            selectedShape = "Freehand";
            eraserActive = false;
        });
        toolbarPanel.add(toolBox);

        toolbarPanel.add(Box.createVerticalStrut(10));

        JLabel brushWidthLabel = new JLabel("Brush Width", SwingConstants.CENTER);
        brushWidthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(brushWidthLabel);

        JSlider brushWidthSlider = new JSlider(1, 20, brushWidth);
        brushWidthSlider.setMajorTickSpacing(5);
        brushWidthSlider.setPaintTicks(true);
        brushWidthSlider.setMaximumSize(new Dimension(120, 25));
        brushWidthSlider.addChangeListener(e -> brushWidth = brushWidthSlider.getValue());
        toolbarPanel.add(brushWidthSlider);

        toolbarPanel.add(Box.createVerticalStrut(10));

        JLabel colorLabel = new JLabel("Color", SwingConstants.CENTER);
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(colorLabel);

        // Color button
        JButton colorButton = new JButton("Pick Color");
        colorButton.setMaximumSize(new Dimension(120, 25)); // Align to match dropdown box size
        colorButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Align it like the dropdowns
        toolbarPanel.add(colorButton);
        colorButton.addActionListener(e -> {
            Color chosenColor = JColorChooser.showDialog(null, "Choose a color", selectedColor);
            if (chosenColor != null) {
                selectedColor = chosenColor;
            }
        });

        toolbarPanel.add(Box.createVerticalStrut(10));

        JLabel shapeLabel = new JLabel("Shape", SwingConstants.CENTER);
        shapeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(shapeLabel);

        String[] shapes = {"Freehand", "Line", "Rectangle", "Oval"};
        JComboBox<String> shapeBox = new JComboBox<>(shapes);
        shapeBox.setMaximumSize(new Dimension(120, 25));
        shapeBox.setSelectedItem("Freehand");
        shapeBox.addActionListener(e -> selectedShape = (String) shapeBox.getSelectedItem());
        toolbarPanel.add(shapeBox);

        toolbarPanel.add(Box.createVerticalStrut(10));

        JLabel eraserLabel = new JLabel("Eraser", SwingConstants.CENTER);
        eraserLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(eraserLabel);

        // Eraser button
        JButton eraserButton = new JButton("Eraser");
        eraserButton.setMaximumSize(new Dimension(120, 25)); // Align to match dropdown box size
        eraserButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Align it like the dropdowns
        toolbarPanel.add(eraserButton);
        eraserButton.addActionListener(e -> {
            eraserActive = true;
            selectedShape = "Freehand";
        });

        toolbarPanel.add(Box.createVerticalStrut(10));

        JLabel eraserWidthLabel = new JLabel("Eraser Width", SwingConstants.CENTER);
        eraserWidthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        toolbarPanel.add(eraserWidthLabel);

        JSlider eraserWidthSlider = new JSlider(1, 20, eraserWidth);
        eraserWidthSlider.setMajorTickSpacing(5);
        eraserWidthSlider.setPaintTicks(true);
        eraserWidthSlider.setMaximumSize(new Dimension(120, 25));
        eraserWidthSlider.addChangeListener(e -> eraserWidth = eraserWidthSlider.getValue());
        toolbarPanel.add(eraserWidthSlider);

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
                startX = prevX;
                startY = prevY;
                dragging = true;
                isDrawingShape = !selectedShape.equals("Freehand");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isDrawingShape) {
                    dragging = false;
                }
                drawingPanel.finalizeShape(startX, startY, e.getX(), e.getY());
                isDrawingShape = false;
            }
        });

        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentX = e.getX();
                currentY = e.getY();
                if (dragging) {
                    if (eraserActive) {
                        drawingPanel.erase(prevX, prevY, currentX, currentY);
                    } else if (isDrawingShape) {
                        drawingPanel.previewShape(startX, startY, currentX, currentY);
                    } else {
                        drawingPanel.draw(prevX, prevY, currentX, currentY, selectedTool);
                    }
                    prevX = currentX;
                    prevY = currentY;
                }
            }
        });

        setVisible(true);
    }

    class DrawingPanel extends JPanel {
        private Image image;
        private Graphics2D g2d, previewGraphics;
        private Image previewImage;

        public DrawingPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.WHITE);
        }

        // @Override
        // protected void paintComponent(Graphics g) {
        //     super.paintComponent(g);
        //     if (image == null) {
        //         image = createImage(getWidth(), getHeight());
        //         g2d = (Graphics2D) image.getGraphics();
        //         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //         clear();
        //     }
        //     g.drawImage(image, 0, 0, null);  // Main image (permanent drawings)
        //     if (previewImage != null) {
        //         g.drawImage(previewImage, 0, 0, null);  // Temporary drawing (for shape previews)
        //     }
        // }

        public void clear() {
            g2d.setPaint(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setPaint(selectedColor);
            repaint();
        }

        public void draw(int x1, int y1, int x2, int y2, String tool) {
            g2d.setPaint(selectedColor);

            if (selectedShape.equals("Freehand")) {
                switch (tool) {
                    case "Pencil":
                        g2d.fillOval(x1 - brushWidth / 2, y1 - brushWidth / 2, brushWidth, brushWidth);
                        break;
                    case "Spray":
                        spray(x1, y1);  // Spray effect
                        return;  // No need for line drawing with spray
                    case "Brush":
                        g2d.setStroke(new BasicStroke(brushWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2d.drawLine(x1, y1, x2, y2);  // Smooth line for brush
                        break;
                    case "Marker":
                        g2d.setStroke(new BasicStroke(brushWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                        g2d.drawLine(x1, y1, x2, y2);  // Hard-edged line
                        break;
                    case "Highlighter":
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                        g2d.setStroke(new BasicStroke(brushWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                        g2d.drawLine(x1, y1, x2, y2);  // Transparent effect
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));  // Reset transparency
                        break;
                }
            }
            repaint();
        }

        public void previewShape(int startX, int startY, int x2, int y2) {
            // Recreate the preview image every time with transparency
            previewImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            previewGraphics = (Graphics2D) previewImage.getGraphics();
            previewGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
            // Now draw the preview shape without altering the background
            previewGraphics.setPaint(selectedColor);
            previewGraphics.setStroke(new BasicStroke(brushWidth));
        
            switch (selectedShape) {
                case "Line":
                    previewGraphics.drawLine(startX, startY, x2, y2);
                    break;
                case "Rectangle":
                    previewGraphics.drawRect(Math.min(startX, x2), Math.min(startY, y2),
                            Math.abs(startX - x2), Math.abs(startY - y2));
                    break;
                case "Oval":
                    previewGraphics.drawOval(Math.min(startX, x2), Math.min(startY, y2),
                            Math.abs(startX - x2), Math.abs(startY - y2));
                    break;
            }
            repaint(); // Repaint to display the preview image
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        
            // Draw the permanent image (existing drawings)
            if (image == null) {
                image = createImage(getWidth(), getHeight());
                g2d = (Graphics2D) image.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                clear();
            }
            g.drawImage(image, 0, 0, null);  // Main image (permanent drawings)
        
            // Draw the preview image (temporary drawing for shape previews)
            if (previewImage != null) {
                g.drawImage(previewImage, 0, 0, null);  // Overlay preview on top of permanent image
            }
        }
                

        public void finalizeShape(int startX, int startY, int x2, int y2) {
            if (selectedShape.equals("Freehand")) {
                return;
            }
            g2d.setPaint(selectedColor);
            g2d.setStroke(new BasicStroke(brushWidth));
            switch (selectedShape) {
                case "Line":
                    g2d.drawLine(startX, startY, x2, y2);
                    break;
                case "Rectangle":
                    g2d.drawRect(Math.min(startX, x2), Math.min(startY, y2),
                            Math.abs(startX - x2), Math.abs(startY - y2));
                    break;
                case "Oval":
                    g2d.drawOval(Math.min(startX, x2), Math.min(startY, y2),
                            Math.abs(startX - x2), Math.abs(startY - y2));
                    break;
            }
            previewImage = null;  // Clear the preview layer after finalizing
            repaint();
        }

        public void erase(int x1, int y1, int x2, int y2) {
            g2d.setPaint(Color.WHITE);
            g2d.setStroke(new BasicStroke(eraserWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.drawLine(x1, y1, x2, y2);  // Freehand erase
            repaint();
        }

        private void spray(int x, int y) {
            Random rand = new Random();
            for (int i = 0; i < sprayDensity; i++) {
                int dx = rand.nextInt(brushWidth) - brushWidth / 2;
                int dy = rand.nextInt(brushWidth) - brushWidth / 2;
                g2d.fillOval(x + dx, y + dy, 1, 1);  // Draw a random dot
            }
            repaint();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PaintApp::new);
    }
}
