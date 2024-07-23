import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.*;

public class Main extends JFrame{
    private JDesktopPane theDesktop;
    private int[][] matR, matG, matB;
    private BufferedImage imgPessoa;
    private BufferedImage imgPaisagem;
    JFileChooser fc  = new JFileChooser();
    String path = "";
    String pathPaisagem = "";
    String pathPessoa = "";

    public void geraImagemEqualizada(int[][] matRed, int[][] matGreen, int[][] matBlue, int[] histogramaEqualizado) {
        int width = matRed[0].length;
        int height = matRed.length;

        int[] pixels = new int[width * height * 3];
        int pos = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int red = histogramaEqualizado[matRed[i][j]];
                int green = histogramaEqualizado[matGreen[i][j]];
                int blue = histogramaEqualizado[matBlue[i][j]];

                red = Math.max(0, Math.min(255, red));
                green = Math.max(0, Math.min(255, green));
                blue = Math.max(0, Math.min(255, blue));

                pixels[pos] = red;
                pixels[pos + 1] = green;
                pixels[pos + 2] = blue;
                pos += 3;
            }
        }

        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = equalizedImage.getRaster();
        raster.setPixels(0, 0, width, height, pixels);

        JFrame frame = new JFrame("Imagem Equalizada");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(width, height);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(equalizedImage, 0, 0, null);
            }
        };

        frame.add(panel);
        frame.setVisible(true);
    }

    private int obterTotalPixels() {
        int totalPixels = 0;

        int[][] matRed = obterMatrizVermelha();
        int height = matRed.length;
        int width = matRed[0].length;

        totalPixels = height * width;

        return totalPixels;
    }

    public void equalizarHistograma(int[] histograma){

        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int totalPixels = obterTotalPixels();

        double[] probabilidade = new double[256];

        for (int i = 0; i < histograma.length; i++) {
            probabilidade[i] = (double) histograma[i] / totalPixels;
        }

        double soma = 0;
        double[] probabilidadeAcumulada = new double[256];
        for (int i = 0; i < probabilidade.length; i++) {
            if(i==0) {
                continue;
            } else {
                soma += probabilidade[i -1];
            }

            probabilidadeAcumulada[i] = probabilidade[i]+soma;
        }

        for (int i = 0; i < probabilidadeAcumulada.length; i++) {
            probabilidadeAcumulada[i] = probabilidadeAcumulada[i] * 255;
        }

        int[] histogramaEqualizado = new int[256];
        for (int i = 0; i < probabilidadeAcumulada.length; i++) {
            histogramaEqualizado[i] = (int) probabilidadeAcumulada[i];
        }

//        System.out.println("Histograma Equalizado");
//        for (int i = 0; i < histogramaEqualizado.length; i++) {
//            System.out.println(histogramaEqualizado[i]);
//        }

        JFrame frame = new JFrame("Histograma Equalizado");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(840, 840);
        frame.setVisible(true);

        JPanel panel = new JPanel(){
            @Override
            public void paintComponent(java.awt.Graphics g){
                super.paintComponent(g);
                for (int i = 0; i < probabilidadeAcumulada.length; i++) {
                    g.drawLine(i * 3, 255 * 3, i * 3, (int) (255 * 3 - histogramaEqualizado[i]));
                }
            }
        };

        frame.add(panel);
        geraImagemEqualizada(matRed, matGreen, matBlue, histogramaEqualizado);
    }

    public void geraHistograma(){
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int step = 2;

        int[][] histoPlot = new int[256*step][256*step];

        int[] histograma = new int[256];

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                histograma[media]++;
            }
        }

        //criar gráfico com hisograma
        JFrame frame = new JFrame("Histograma");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(840, 840);
        frame.setVisible(true);

        JPanel panel = new JPanel(){
            @Override
            public void paintComponent(java.awt.Graphics g){
                super.paintComponent(g);
                for(int i = 0; i < histograma.length; i++){
                    g.drawLine(i*3, 255*3, i*3, 255*3 - histograma[i]);
                }
            }
        };

        frame.add(panel);
        equalizarHistograma(histograma);
    }

    public void converte_hsv(){
        //converter de RGB para HSV
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int[][] matH = new int[matRed.length][matRed[0].length];
        int[][] matS = new int[matRed.length][matRed[0].length];
        int[][] matV = new int[matRed.length][matRed[0].length];

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                double r = matRed[i][j] / 255.0;
                double g = matGreen[i][j] / 255.0;
                double b = matBlue[i][j] / 255.0;

                double cmax = Math.max(r, Math.max(g, b));
                double cmin = Math.min(r, Math.min(g, b));
                double delta = cmax - cmin;

                double h = 0;
                if (delta == 0) {
                    h = 0;
                } else if (cmax == r) {
                    h = (int)((60 * (((g - b) / delta) % 6)));
                } else if (cmax == g) {
                    h = (int)((60 * (((b - r) / delta) + 2)));
                } else if (cmax == b) {
                    h = (int)((60 * (((r - g) / delta) + 4)));
                }

                if (h < 0) {
                    h += 360;
                }

                double s = (cmax == 0) ? 0 : (delta / cmax);
                double v = cmax;

                matH[i][j] = (int)(h / 2);
                matS[i][j] = (int)(s * 100);
                matV[i][j] = (int)(v * 100);
            }
        }
        hsvtorgb(matH, matS, matV, "H");
        hsvtorgb(matH, matS, matV, "S");
        hsvtorgb(matH, matS, matV, "V");
    }

    public void hsvtorgb(int[][] matComponent, int[][] matS, int[][] matV, String componentToKeep) {
        //converter de HSV para RGB
        int[][] matRed = new int[matComponent.length][matComponent[0].length];
        int[][] matGreen = new int[matComponent.length][matComponent[0].length];
        int[][] matBlue = new int[matComponent.length][matComponent[0].length];

        for (int i = 0; i < matComponent.length; i++) {
            for (int j = 0; j < matComponent[0].length; j++) {
                double h = matComponent[i][j] * 2;

                double s = matS[i][j] / 100.0;
                double v = matV[i][j] / 100.0;

                double c = v * s;
                double x = c * (1 - Math.abs((h / 60) % 2 - 1));
                double m = v - c;

                double r = 0, g = 0, b = 0;
                if (h >= 0 && h < 60) {
                    r = c;
                    g = x;
                    b = 0;
                } else if (h >= 60 && h < 120) {
                    r = x;
                    g = c;
                    b = 0;
                } else if (h >= 120 && h < 180) {
                    r = 0;
                    g = c;
                    b = x;
                } else if (h >= 180 && h < 240) {
                    r = 0;
                    g = x;
                    b = c;
                } else if (h >= 240 && h < 300) {
                    r = x;
                    g = 0;
                    b = c;
                } else if (h >= 300 && h < 360) {
                    r = c;
                    g = 0;
                    b = x;
                }

                if (componentToKeep.equals("H")) {
                    r = matComponent[i][j];
                    g = (int)((r + m) * 255);
                    b = (int)((r + m) * 255);
                } else if (componentToKeep.equals("S")) {
                    g = matComponent[i][j];
                    r = (int)((g + m) * 255);
                    b = (int)((g + m) * 255);
                } else if (componentToKeep.equals("V")) {
                    b = matComponent[i][j];
                    r = (int)((b + m) * 255);
                    g = (int)((b + m) * 255);
                }

                matRed[i][j] = (int)((r + m) * 255);
                matGreen[i][j] = (int)((g + m) * 255);
                matBlue[i][j] = (int)((b + m) * 255);
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void escalaCinza()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();


        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                matRed[i][j] = media;
                matGreen[i][j] = media;
                matBlue[i][j] = media;
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void imagemBinaria()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int limiar = 127;

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                int novoPixel = media > limiar ? 255 : 0;
                matRed[i][j] = novoPixel;
                matGreen[i][j] = novoPixel;
                matBlue[i][j] = novoPixel;
            }
        }

         geraImagem(matRed, matGreen, matBlue);
    }

    public void imagemNegativa()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                matRed[i][j] = 255 - matRed[i][j];
                matGreen[i][j] = 255 - matGreen[i][j];
                matBlue[i][j] = 255 - matBlue[i][j];
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void corDominante()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int max = Math.max(matRed[i][j], Math.max(matGreen[i][j], matBlue[i][j]));
                if(matRed[i][j] == max){
                    matRed[i][j] = 255;
                    matGreen[i][j] = 0;
                    matBlue[i][j] = 0;
                }else  if(matGreen[i][j] == max){
                    matRed[i][j] = 0;
                    matGreen[i][j] = 255;
                }else if(matBlue[i][j] == max){
                    matRed[i][j] = 0;
                    matGreen[i][j] = 0;
                    matBlue[i][j] = 255;
                }else {
                    matRed[i][j] = 255;
                    matGreen[i][j] = 255;
                    matBlue[i][j] = 255;
                }
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }


    public void escalaCinzaEscuro()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int indiceEscurecimento = 50;

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                int escuro = (media - indiceEscurecimento) > 0 ? media - indiceEscurecimento : 0;
                matRed[i][j] = escuro;
                matGreen[i][j] = escuro;
                matBlue[i][j] = escuro;
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }


    public void escalaCinzaClaro()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int indiceClareamento = 50;

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                matRed[i][j] = Math.min(255, media + indiceClareamento);
                matGreen[i][j] = Math.min(255, media + indiceClareamento);
                matBlue[i][j] = Math.min(255, media + indiceClareamento);
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void mantemRed()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int linear = 167;
        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                if (matRed[i][j] < linear)
                    matRed[i][j] = media;
                matGreen[i][j] = media;
                matBlue[i][j] = media;
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void mantemGreen()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int linear = 167;
        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                if (matGreen[i][j] < linear)
                    matGreen[i][j] = media;
                matRed[i][j] = media;
                matBlue[i][j] = media;
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void mantemBlue()
    {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int linear = 167;
        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                if (matBlue[i][j] < linear)
                    matBlue[i][j] = media;
                matRed[i][j] = media;
                matGreen[i][j] = media;
            }
        }

        geraImagem(matRed, matGreen, matBlue);
    }

    public void removeRed()
    {
        int[][] matRed = obterMatrizVermelha();
        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                matRed[i][j] = 0;
            }
        }

        geraImagem(matRed, obterMatrizVerde(), obterMatrizAzul());
    }

    public void removeGreen()
    {

        int[][] matGreen = obterMatrizVerde();
        for(int i = 0; i < matGreen.length; i++){
            for(int j = 0; j < matGreen[0].length; j++){
                matGreen[i][j] = 0;
            }
        }

        geraImagem(obterMatrizVermelha(), matGreen, obterMatrizAzul());
    }

    public void removeBlue()
    {
        int[][] matBlue = obterMatrizAzul();
        for(int i = 0; i < matBlue.length; i++){
            for(int j = 0; j < matBlue[0].length; j++){
                matBlue[i][j] = 0;
            }
        }

        geraImagem(obterMatrizVermelha(), obterMatrizVerde(), matBlue);
    }

    public static int countWhitePixelsVertical(int[][] redMatrix, int[][] greenMatrix, int[][] blueMatrix) {
        int whitePixels = 0;

        int metade = redMatrix[0].length / 2;
        

        for (int x = 0; x < redMatrix.length; x++) {
            if (redMatrix[x][metade] == 255 && greenMatrix[x][metade] == 255 && blueMatrix[x][metade] == 255) {
                whitePixels++;
            }
        }

        return whitePixels;
    }

    public static int  countWhitePixelsHorizontal(int[][] redMatrix, int[][] greenMatrix, int[][] blueMatrix) {
        int blackPixels = 0;

        int metade = redMatrix.length / 2;

        for (int x = 0; x < redMatrix[0].length; x++) {
            if (redMatrix[metade][x] == 255 && greenMatrix[metade][x] == 255 && blueMatrix[metade][x] == 255) {
                blackPixels++;
            }
        }

        return blackPixels;
    }

    public void redimensionarImagem(double escala){

        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int alturaOriginal = matRed.length;
        int larguraOriginal = matRed[0].length;

        int novaAltura = (int) (alturaOriginal * escala);
        int novaLargura = (int) (larguraOriginal * escala);

        int[][] novaMatRed = new int[novaAltura][novaLargura];
        int[][] novaMatGreen = new int[novaAltura][novaLargura];
        int[][] novaMatBlue = new int[novaAltura][novaLargura];

        for(int i = 0; i < novaAltura; i++) {
            for(int j = 0; j < novaLargura; j++) {
                int pixelAlturaOriginal = (int) (i / escala);
                int pixelLarguraOriginal = (int) (j / escala);

                novaMatRed[i][j] = matRed[pixelAlturaOriginal][pixelLarguraOriginal];
                novaMatGreen[i][j] = matGreen[pixelAlturaOriginal][pixelLarguraOriginal];
                novaMatBlue[i][j] = matBlue[pixelAlturaOriginal][pixelLarguraOriginal];
            }
        }

        geraImagem(novaMatRed, novaMatGreen, novaMatBlue);
    }

    public void removeFundo(){
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();
        int limiar = 127;

        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                int media = (matRed[i][j] + matGreen[i][j] + matBlue[i][j])/3;
                int novoPixel = media < limiar ? 255 : 0;
                matRed[i][j] = novoPixel;
                matGreen[i][j] = novoPixel;
                matBlue[i][j] = novoPixel;
            }
        }

        int vertical = countWhitePixelsVertical(matRed, matGreen, matBlue);
        int horizontal = countWhitePixelsHorizontal(matRed, matGreen, matBlue);

        int porcentagem = (vertical * 100) / (matRed.length);

        System.out.println("Horizontal: " + horizontal);
        System.out.println("Vertical: " + vertical);
        System.out.println("Porcentagem: " + porcentagem);

        geraImagem(matRed, matGreen, matBlue);

        if (porcentagem < 50) {
            JOptionPane.showMessageDialog(null, "Caneta: " + porcentagem + "%");
        }else{
            JOptionPane.showMessageDialog(null, "Celular: " + porcentagem + "%");
        }
    }

    public void rotacionarImagem(boolean sentidoHorario){
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int [][] novaRed = new int[matRed[0].length][matRed.length];
        int [][] novaGreen = new int[matGreen[0].length][matGreen.length];
        int [][] novaBlue = new int[matBlue[0].length][matBlue.length];

        //Rotaciona a imagem
        for(int i = 0; i < matRed.length; i++){
            for(int j = 0; j < matRed[0].length; j++){
                if(sentidoHorario){
                    novaRed[j][i] = matRed[matRed.length - i - 1][matRed[0].length - j - 1];
                    novaGreen[j][i] = matGreen[matGreen.length - i - 1][matGreen[0].length - j - 1];
                    novaBlue[j][i] = matBlue[matBlue.length - i - 1][matBlue[0].length - j - 1];

                }else{
                    novaRed[j][i] = matRed[i][j];
                    novaGreen[j][i] = matGreen[i][j];
                    novaBlue[j][i] = matBlue[i][j];

                }
            }
        }

        geraImagem(novaRed, novaGreen, novaBlue);
    }

    public void rotacionarImagemByGrau(double grau) {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int altura = matRed.length;
        int largura = matRed[0].length;

        int centroX = largura / 2;
        int centroY = altura / 2;

        double cos = Math.cos(Math.toRadians(grau));
        double sen = Math.sin(Math.toRadians(grau));

        int novaLargura = (int) Math.ceil(Math.abs(largura * cos) + Math.abs(altura * sen));
        int novaAltura = (int) Math.ceil(Math.abs(largura * sen) + Math.abs(altura * cos));

        int novoCentroX = novaLargura / 2;
        int novoCentroY = novaAltura / 2;

        int[][] novaRed = new int[novaAltura][novaLargura];
        int[][] novaGreen = new int[novaAltura][novaLargura];
        int[][] novaBlue = new int[novaAltura][novaLargura];

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int dx = x - centroX;
                int dy = y - centroY;

                int novoX = (int) (dx * cos - dy * sen) + novoCentroX;
                int novoY = (int) (dx * sen + dy * cos) + novoCentroY;

                if (novoX >= 0 && novoX < novaLargura && novoY >= 0 && novoY < novaAltura) {
                    novaRed[novoY][novoX] = matRed[y][x];
                    novaGreen[novoY][novoX] = matGreen[y][x];
                    novaBlue[novoY][novoX] = matBlue[y][x];
                }
            }
        }
        geraImagem(novaRed, novaGreen, novaBlue);
    }

    public void filtroGradienteSobel() {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int altura = matRed.length;
        int largura = matRed[0].length;

        int[][] novaRed = new int[altura][largura];
        int[][] novaGreen = new int[altura][largura];
        int[][] novaBlue = new int[altura][largura];


        int[][] kernelX = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };

        int[][] kernelY = {
                {-1, -2, -1},
                {0, 0, 0},
                {1, 2, 1}
        };


        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {

                int gradientXRed = 0;
                int gradientYRed = 0;


                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        gradientXRed += matRed[y + ky - 1][x + kx - 1] * kernelX[ky][kx];
                        gradientYRed += matRed[y + ky - 1][x + kx - 1] * kernelY[ky][kx];
                    }
                }


                int magnitudeRed = (int) Math.sqrt(gradientXRed * gradientXRed + gradientYRed * gradientYRed);


                int gradientXGreen = 0;
                int gradientYGreen = 0;


                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        gradientXGreen += matGreen[y + ky - 1][x + kx - 1] * kernelX[ky][kx];
                        gradientYGreen += matGreen[y + ky - 1][x + kx - 1] * kernelY[ky][kx];
                    }
                }


                int magnitudeGreen = (int) Math.sqrt(gradientXGreen * gradientXGreen + gradientYGreen * gradientYGreen);


                int gradientXBlue = 0;
                int gradientYBlue = 0;


                for (int ky = 0; ky < 3; ky++) {
                    for (int kx = 0; kx < 3; kx++) {
                        gradientXBlue += matBlue[y + ky - 1][x + kx - 1] * kernelX[ky][kx];
                        gradientYBlue += matBlue[y + ky - 1][x + kx - 1] * kernelY[ky][kx];
                    }
                }

                int magnitudeBlue = (int) Math.sqrt(gradientXBlue * gradientXBlue + gradientYBlue * gradientYBlue);

                novaRed[y][x] = magnitudeRed;
                novaGreen[y][x] = magnitudeGreen;
                novaBlue[y][x] = magnitudeBlue;
            }
        }

        geraImagem(novaRed, novaGreen, novaBlue);
    }

    public void filtroGaussiano() {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int altura = matRed.length;
        int largura = matRed[0].length;

        int[][] novaRed = new int[altura][largura];
        int[][] novaGreen = new int[altura][largura];
        int[][] novaBlue = new int[altura][largura];


        double[][] kernel = {
                {1,  4,  7,  4,  1},
                {4, 16, 26, 16,  4},
                {7, 26, 41, 26,  7},
                {4, 16, 26, 16,  4},
                {1,  4,  7,  4,  1}
        };

        double fator = 1.0 / 273.0;


        for (int y = 2; y < altura - 2; y++) {
            for (int x = 2; x < largura - 2; x++) {
                double sumRed = 0;
                double sumGreen = 0;
                double sumBlue = 0;


                for (int ky = 0; ky < 5; ky++) {
                    for (int kx = 0; kx < 5; kx++) {
                        int pixelY = y + ky - 2;
                        int pixelX = x + kx - 2;

                        sumRed += matRed[pixelY][pixelX] * kernel[ky][kx];
                        sumGreen += matGreen[pixelY][pixelX] * kernel[ky][kx];
                        sumBlue += matBlue[pixelY][pixelX] * kernel[ky][kx];
                    }
                }

                novaRed[y][x] = (int) Math.round(sumRed * fator);
                novaGreen[y][x] = (int) Math.round(sumGreen * fator);
                novaBlue[y][x] = (int) Math.round(sumBlue * fator);
            }
        }

        geraImagem(novaRed, novaGreen, novaBlue);
    }

    public void filtroMedia() {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int altura = matRed.length;
        int largura = matRed[0].length;

        int[][] novaRed = new int[altura][largura];
        int[][] novaGreen = new int[altura][largura];
        int[][] novaBlue = new int[altura][largura];


        int[][] kernel = {
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        };

        int kernelSize = 5;
        int kernelSum = 25;


        for (int y = 2; y < altura - 2; y++)
            for (int x = 2; x < largura - 2; x++) {
                int sumRed = 0;
                int sumGreen = 0;
                int sumBlue = 0;


                for (int ky = 0; ky < kernelSize; ky++) {
                    for (int kx = 0; kx < kernelSize; kx++) {
                        int pixelY = y + ky - 2;
                        int pixelX = x + kx - 2;

                        sumRed += matRed[pixelY][pixelX] * kernel[ky][kx];
                        sumGreen += matGreen[pixelY][pixelX] * kernel[ky][kx];
                        sumBlue += matBlue[pixelY][pixelX] * kernel[ky][kx];
                    }
                }

                novaRed[y][x] = sumRed / kernelSum;
                novaGreen[y][x] = sumGreen / kernelSum;
                novaBlue[y][x] = sumBlue / kernelSum;
            }

        geraImagem(novaRed, novaGreen, novaBlue);
    }

    public void filtroMediana() {
        int[][] matRed = obterMatrizVermelha();
        int[][] matGreen = obterMatrizVerde();
        int[][] matBlue = obterMatrizAzul();

        int altura = matRed.length;
        int largura = matRed[0].length;

        int[][] novaRed = new int[altura][largura];
        int[][] novaGreen = new int[altura][largura];
        int[][] novaBlue = new int[altura][largura];

        // Aplicar o filtro da mediana
        for (int y = 2; y < altura - 2; y++) {
            for (int x = 2; x < largura - 2; x++) {
                int[] vizinhosRed = new int[25];
                int[] vizinhosGreen = new int[25];
                int[] vizinhosBlue = new int[25];

                int index = 0;
                for (int ky = -2; ky <= 2; ky++) {
                    for (int kx = -2; kx <= 2; kx++) {
                        vizinhosRed[index] = matRed[y + ky][x + kx];
                        vizinhosGreen[index] = matGreen[y + ky][x + kx];
                        vizinhosBlue[index] = matBlue[y + ky][x + kx];
                        index++;
                    }
                }

                Arrays.sort(vizinhosRed);
                Arrays.sort(vizinhosGreen);
                Arrays.sort(vizinhosBlue);

                novaRed[y][x] = vizinhosRed[12];  // Mediana de uma lista de 25 elementos é o 13º elemento
                novaGreen[y][x] = vizinhosGreen[12];
                novaBlue[y][x] = vizinhosBlue[12];
            }
        }

        geraImagem(novaRed, novaGreen, novaBlue);
    }

    public int [][][] obterMatrizRGB(BufferedImage imagem){
        int altura = imagem.getHeight();
        int largura = imagem.getWidth();

        int [][][] matrizRGB = new int[largura][altura][3];

        for(int i = 0; i < largura; i++){
            for(int j = 0; j < altura; j++){
                int rgb = imagem.getRGB(i, j);
                matrizRGB[i][j][0] = (rgb >> 16) & 0xFF;
                matrizRGB[i][j][1] = (rgb >> 8) & 0xFF;
                matrizRGB[i][j][2] = rgb & 0xFF;
            }
        }

        return matrizRGB;
    }

    public int [][][] alterarFundo_toTransparente(int [][][] matrizRGB){
        int altura = matrizRGB[0].length;
        int largura = matrizRGB.length;

        int [][][] novaMatrizRGB = new int[largura][altura][4];

        for(int i = 0; i < largura; i++){
            for(int j = 0; j < altura; j++){
                int r = matrizRGB[i][j][0];
                int g = matrizRGB[i][j][1];
                int b = matrizRGB[i][j][2];

                if(r > 200 && g > 200 && b > 200){
                    novaMatrizRGB[i][j][0] = 0;
                    novaMatrizRGB[i][j][1] = 0;
                    novaMatrizRGB[i][j][2] = 0;
                    novaMatrizRGB[i][j][3] = 0;
                }else{
                    novaMatrizRGB[i][j][0] = r;
                    novaMatrizRGB[i][j][1] = g;
                    novaMatrizRGB[i][j][2] = b;
                    novaMatrizRGB[i][j][3] = 255;
                }
            }
        }

        return novaMatrizRGB;
    }

    public int[][][] juntarImagens(int[][][] matrizPessoa, int[][][] matrizPaisagem) {
        int alturaPaisagem = matrizPaisagem[0].length;
        int larguraPaisagem = matrizPaisagem.length;

        int[][][] novaMatriz = new int[larguraPaisagem][alturaPaisagem][4];

        for (int i = 0; i < larguraPaisagem; i++) {
            for (int j = 0; j < alturaPaisagem; j++) {
                novaMatriz[i][j][0] = matrizPaisagem[i][j][0];
                novaMatriz[i][j][1] = matrizPaisagem[i][j][1];
                novaMatriz[i][j][2] = matrizPaisagem[i][j][2];
                novaMatriz[i][j][3] = 255;
            }
        }

        int alturaPessoa = matrizPessoa[0].length;
        int larguraPessoa = matrizPessoa.length;

        int inicioI = (larguraPaisagem - larguraPessoa) / 2;
        int inicioJ = (alturaPaisagem - alturaPessoa) / 2;

        for (int i = 0; i < larguraPessoa; i++) {
            for (int j = 0; j < alturaPessoa; j++) {
                if (inicioI + i < larguraPaisagem && inicioJ + j < alturaPaisagem) {
                    if (matrizPessoa[i][j][3] > 0) {
                        novaMatriz[inicioI + i][inicioJ + j][0] = matrizPessoa[i][j][0];
                        novaMatriz[inicioI + i][inicioJ + j][1] = matrizPessoa[i][j][1];
                        novaMatriz[inicioI + i][inicioJ + j][2] = matrizPessoa[i][j][2];
                        novaMatriz[inicioI + i][inicioJ + j][3] = matrizPessoa[i][j][3];
                    }
                }
            }
        }

        return novaMatriz;
    }

    public int[][][] filtroMediaDinamico(int[][][] matrizRGB, int tamanhoKernel){
        int altura = matrizRGB[0].length;
        int largura = matrizRGB.length;

        int [][][] novaMatriz = new int[largura][altura][3];
        int[][] kernel = new int [tamanhoKernel][tamanhoKernel];

        for(int i=0; i<tamanhoKernel; i++){
            for(int j=0; j<tamanhoKernel; j++){
                kernel[i][j] = 1;
            }
        }

        int kernelSum = (tamanhoKernel * tamanhoKernel);
        int desvio = tamanhoKernel / 2;

        for(int i = desvio; i < largura - desvio; i++){
            for(int j = desvio; j < altura - desvio; j++){
                int somaR = 0;
                int somaG = 0;
                int somaB = 0;

                for(int k = 0; k < tamanhoKernel; k++){
                    for(int l = 0; l < tamanhoKernel; l++){
                        int pixelX = i + l - desvio;
                        int pixelY = j + k - desvio;

                        somaR += matrizRGB[pixelX][pixelY][0] * kernel[k][l];
                        somaG += matrizRGB[pixelX][pixelY][1] * kernel[k][l];
                        somaB += matrizRGB[pixelX][pixelY][2] * kernel[k][l];
                    }
                }

                novaMatriz[i][j][0] = somaR / kernelSum;
                novaMatriz[i][j][1] = somaG / kernelSum;
                novaMatriz[i][j][2] = somaB / kernelSum;
            }
        }

        return novaMatriz;
    }

    public void juntarPessoaPaisagem(){
        System.out.println(imgPessoa);
        if(imgPessoa == null){
            JOptionPane.showMessageDialog(null, "Selecione uma imagem de pessoa");
            return;
        }

        if (imgPaisagem == null){
            JOptionPane.showMessageDialog(null, "Selecione uma imagem de paisagem");
            return;
        }

        int [][][] matrizPessoa = obterMatrizRGB(imgPessoa);
        int [][][] matrizPaisagem = obterMatrizRGB(imgPaisagem);

        int[][][] matrrizPessoaSemFundo = alterarFundo_toTransparente(matrizPessoa);
        int[][][] novaMatriz = juntarImagens(matrrizPessoaSemFundo, matrizPaisagem);

        exibirImagemProcessada(novaMatriz, "Pessoa na paisagem");
    }

    public Main(){
        super("PhotoIFMG");
        JMenuBar bar = new JMenuBar();
        JMenu addMenu = new JMenu("Abrir");
        JMenuItem fileItem = new JMenuItem("Abir uma imagem de arquivo");
        JMenu opcaoTipoImagem = new JMenu("Escolher tipo de imagem");
        JMenuItem opcaoImagemPaisagem = new JMenuItem("Abrir Imagem de paisagem");
        JMenuItem opcaoImagemPessoa = new JMenuItem("Abrir Imagem de pessoa");
        JMenuItem newFrame = new JMenuItem("Internal Frame");

        opcaoTipoImagem.add(opcaoImagemPaisagem);
        opcaoTipoImagem.add(opcaoImagemPessoa);

        addMenu.add(fileItem);
        addMenu.add(opcaoTipoImagem);
        addMenu.add(newFrame);
        bar.add(addMenu);

        JMenu addMenu2 = new JMenu("Processar");
        JMenuItem item1 = new JMenuItem("Escala de cinza");
        JMenuItem item2 = new JMenuItem("Imagem binária");
        JMenuItem item3 = new JMenuItem("Negativa");
        JMenuItem item4 = new JMenuItem("Cor dominante");
        JMenuItem item5 = new JMenuItem("Cinza escuro");
        JMenuItem item6 = new JMenuItem("Cinza claro");
        JMenuItem item7 = new JMenuItem("Red");
        JMenuItem item8 = new JMenuItem("Green");
        JMenuItem item9 = new JMenuItem("Blue");
        JMenuItem item10 = new JMenuItem("Remover Red");
        JMenuItem item11 = new JMenuItem("Remover Green");
        JMenuItem item12 = new JMenuItem("Remover Blue");
        JMenuItem item13 = new JMenuItem("Defina o objeto");
        JMenuItem item18 = new JMenuItem("Converter para HSV");
        JMenuItem item19 = new JMenuItem("Gera Histograma");
        JMenuItem item20 = new JMenuItem("Rotação");
        JMenuItem item21 = new JMenuItem("Suavizar Imagem");
        JMenuItem item22 = new JMenuItem("Filtro Gaussiano");
        JMenuItem item23 = new JMenuItem("Filtro Sobel");
        JMenuItem item24 = new JMenuItem("Filtro Mediana");
        JMenuItem item25 = new JMenuItem("Juntar Imagens");
        JMenuItem item26 = new JMenuItem("Aplicar Filtro sobre a imagem juntada");

        JMenu addMenu3 = new JMenu("Redimensionar a imagem");
        JMenuItem item14 = new JMenuItem("Diminuir a imagem");
        JMenuItem item15 = new JMenuItem("Aumentar a imagem");

        JMenu addMenu4 = new JMenu("Rotacionar Imagem");
        JMenuItem item16 = new JMenuItem("Sentido Horário");
        JMenuItem item17 = new JMenuItem("Sentido Anti-Horário");

        addMenu2.add(item1);
        addMenu2.add(item2);
        addMenu2.add(item3);
        addMenu2.add(item4);
        addMenu2.add(item5);
        addMenu2.add(item6);
        addMenu2.add(item7);
        addMenu2.add(item8);
        addMenu2.add(item9);
        addMenu2.add(item10);
        addMenu2.add(item11);
        addMenu2.add(item12);
        addMenu2.add(item13);
        addMenu2.add(item16);
        addMenu2.add(item18);
        addMenu2.add(item19);
        addMenu2.add(item20);
        addMenu2.add(item21);
        addMenu2.add(item22);
        addMenu2.add(item23);
        addMenu2.add(item24);
        addMenu2.add(item25);
        addMenu2.add(item26);

        addMenu3.add(item14);
        addMenu3.add(item15);

        addMenu4.add(item16);
        addMenu4.add(item17);

        bar.add(addMenu2);
        bar.add(addMenu3);
        bar.add(addMenu4);

        setJMenuBar(bar);

        theDesktop = new JDesktopPane();
        getContentPane().add(theDesktop);

        newFrame.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JInternalFrame frame = new JInternalFrame("Exemplo", true,
                                true, true, true);
                        Container container = frame.getContentPane();
                        MyJPanel panel = new MyJPanel();
                        container.add(panel, BorderLayout.CENTER);

                        frame.pack();
                        theDesktop.add(frame);
                        frame.setVisible(true);


                    }
                }

        );

        //ler imagem
        fileItem.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int result = fc.showOpenDialog(null);
                        if(result == JFileChooser.CANCEL_OPTION){
                            return;
                        }
                        path = fc.getSelectedFile().getAbsolutePath();

                        JInternalFrame frame = new JInternalFrame("Exemplo", true,
                                true, true, true);
                        Container container = frame.getContentPane();
                        MyJPanel panel = new MyJPanel();
                        container.add(panel, BorderLayout.CENTER);

                        frame.pack();
                        theDesktop.add(frame);
                        frame.setVisible(true);
                    }
                }

        );

        opcaoImagemPaisagem.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int result = fc.showOpenDialog(null);
                        if(result == JFileChooser.CANCEL_OPTION){
                            return;
                        }
                        path = fc.getSelectedFile().getAbsolutePath();

                        try {
                            imgPaisagem = ImageIO.read(fc.getSelectedFile());
                            JInternalFrame frame = new JInternalFrame("Paisagem", true,
                                    true, true, true);
                            Container container = frame.getContentPane();
                            MyJPanel panel = new MyJPanel();
                            container.add(panel, BorderLayout.CENTER);

                            frame.pack();
                            theDesktop.add(frame);
                            frame.setVisible(true);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Erro ao carregar a imagem da paisagem: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
        );

        opcaoImagemPessoa.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int result = fc.showOpenDialog(null);
                        if(result == JFileChooser.CANCEL_OPTION){
                            return;
                        }

                        path = fc.getSelectedFile().getAbsolutePath();

                        try {
                            imgPessoa = ImageIO.read(fc.getSelectedFile());
                            JInternalFrame frame = new JInternalFrame("Pessoa", true,
                                    true, true, true);
                            Container container = frame.getContentPane();
                            MyJPanel panel = new MyJPanel();
                            container.add(panel, BorderLayout.CENTER);

                            frame.pack();
                            theDesktop.add(frame);
                            frame.setVisible(true);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(null, "Erro ao carregar a imagem da pessoa: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
        );


        item1.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);
                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        escalaCinza();

     				/*int[][] mat = rgbMat.elementAt(0);
     				int[][] mat2 = rgbMat.elementAt(1);
     				int[][] mat3 = rgbMat.elementAt(2);

     				geraImagem(mat3, mat2
     						, mat);*/
                    }
                }

        );

        item2.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        imagemBinaria();

                    }
                }

        );

        item3.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        imagemNegativa();
                    }
                }

        );

        item4.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        corDominante();
                    }
                }

        );

        item5.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        escalaCinzaEscuro();
                    }
                }

        );

        item6.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        escalaCinzaClaro();

                    }
                }

        );

        item7.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        mantemRed();

                    }
                }

        );

        item8.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        mantemGreen();

                    }
                }

        );

        item9.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        mantemBlue();

                    }
                }

        );

        item10.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        removeRed();

                    }
                }

        );

        item11.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        removeGreen();

                    }
                }

        );

        item12.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        removeBlue();

                    }
                }

        );

        item13.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        removeFundo();

                    }
                }

        );

        item14.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        String input = JOptionPane.showInputDialog("Digite a escala de redimensionamento em % (por exemplo, 50 para reduzir pela metade):");
                        if (input != null && !input.isEmpty()) {
                            try {
                                double escala = Double.parseDouble(input);
                                if (escala > 0) {
                                    redimensionarImagem(escala / 100);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Digite um valor positivo para a escala de redimensionamento.");
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Digite um número válido para a escala de redimensionamento.");
                            }
                        }
                    }
                }
        );

        item15.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        String input = JOptionPane.showInputDialog("Digite a escala de redimensionament (por exemplo, 2 para dobrar):");
                        if (input != null && !input.isEmpty()) {
                            try {
                                double escala = Double.parseDouble(input);
                                if (escala > 0) {
                                    redimensionarImagem(escala);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Digite um valor positivo para a escala de redimensionamento.");
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Digite um número válido para a escala de redimensionamento.");
                            }
                        }
                    }
                }
        );

        item16.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);
                        rotacionarImagem(true);
                    }
                }
        );

        item17.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);
                        rotacionarImagem(false);
                    }
                }
        );

        item18.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);
                        converte_hsv();
                    }
                }
        );

        item19.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);
                        geraHistograma();
                    }
                }
        );

        item20.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);

                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);
                        String input = JOptionPane.showInputDialog("Digite o ângulo de rotação:");
                        if (input != null && !input.isEmpty()) {
                            try {
                                double grau = Double.parseDouble(input);
                                rotacionarImagemByGrau(grau);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Digite um número válido para o ângulo de rotação.");
                            }
                        }
                    }
                }
        );

        item21.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);
                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        filtroMedia();
                    }
                }
        );

        item22.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);
                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        filtroGaussiano();
                    }
                }
        );

        item23.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);
                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        filtroGradienteSobel();
                    }
                }
        );

        item24.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);
                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        filtroMediana();
                    }
                }
        );

        item25.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        juntarPessoaPaisagem();
                    }
                }
        );

        item26.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Vector<int[][]> rgbMat = getMatrixRGB();
                        matR = rgbMat.elementAt(0);
                        matG = rgbMat.elementAt(1);
                        matB = rgbMat.elementAt(2);

                        String input = JOptionPane.showInputDialog("Digite o tamanho do kernel:");
                        if (input != null && !input.isEmpty()) {
                            try {
                                int tamanhoKernel = Integer.parseInt(input);
                                if (tamanhoKernel > 0) {
                                    int [][][] matrizPessoa = obterMatrizRGB(imgPessoa);
                                    int [][][] matrizPaisagem = obterMatrizRGB(imgPaisagem);

                                    int[][][] matrrizPessoaSemFundo = alterarFundo_toTransparente(matrizPessoa);
                                    int[][][] filtroMediaPaisagem = filtroMediaDinamico(matrizPaisagem, tamanhoKernel);

                                    int[][][] novaMatriz = juntarImagens(matrrizPessoaSemFundo, filtroMediaPaisagem);
                                    exibirImagemProcessada(novaMatriz, "Pessoa na paisagem com filtro");
                                } else {
                                    JOptionPane.showMessageDialog(null, "Digite um valor positivo para o tamanho do kernel.");
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Digite um número válido para o tamanho do kernel.");
                            }
                        }
                    }
                }
        );

        setSize(600, 400);
        setVisible(true);


    }
    //ler matrizes da imagem
    public Vector<int[][]> getMatrixRGB(){
        BufferedImage img;
        int[][] rmat = null;
        int[][] gmat = null;
        int[][] bmat = null;
        try {
            img = ImageIO.read(new File(path));

            int[][] pixelData = new int[img.getHeight() * img.getWidth()][3];
            rmat = new int[img.getHeight()][img.getWidth()];
            gmat = new int[img.getHeight()][img.getWidth()];
            bmat = new int[img.getHeight()][img.getWidth()];

            int counter = 0;
            for(int i = 0; i < img.getHeight(); i++){
                for(int j = 0; j < img.getWidth(); j++){
                    rmat[i][j] = getPixelData(img, j, i)[0];
                    gmat[i][j] = getPixelData(img, j, i)[1];
                    bmat[i][j] = getPixelData(img, j, i)[2];

                    /*for(int k = 0; k < rgb.length; k++){
                        pixelData[counter][k] = rgb[k];
                    }*/

                    counter++;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        Vector<int[][]> rgb = new Vector<int[][]>();
        rgb.add(rmat);
        rgb.add(gmat);
        rgb.add(bmat);

        return rgb;

    }
    //cria imagem da matriz
    private void geraImagem(int matrix1[][], int matrix2[][], int matrix3[][]) {
        int[] pixels = new int[matrix1.length * matrix1[0].length*3];
        BufferedImage image = new BufferedImage(matrix1[0].length, matrix1.length, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        int pos =0;
        for(int i =0; i < matrix1.length; i++){
            for(int j = 0; j < matrix1[0].length; j++){
                pixels[pos] = matrix1[i][j];
                pixels[pos+1] = matrix2[i][j];
                pixels[pos+2] = matrix3[i][j];
                pos+=3;
            }
        }
        raster.setPixels(0, 0, matrix1[0].length, matrix1.length, pixels);

        //Abre Janela da imagem
        JInternalFrame frame = new JInternalFrame("Processada", true,
                true, true, true);
        Container container = frame.getContentPane();
        MyJPanel panel = new MyJPanel();
        panel.setImageIcon(new ImageIcon(image));
        container.add(panel, BorderLayout.CENTER);

        frame.pack();
        theDesktop.add(frame);
        frame.setVisible(true);

    }

    private void exibirImagemProcessada(int[][][] matrizRGBA, String titulo) {
        int largura = matrizRGBA.length;
        int altura = matrizRGBA[0].length;

        BufferedImage imagemProcessada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int[] pixel = matrizRGBA[x][y];
                int r = pixel[0];
                int g = pixel[1];
                int b = pixel[2];
                int a = pixel[3];

                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                imagemProcessada.setRGB(x, y, argb);
            }
        }

        exibirImagemSelecionada(imagemProcessada, titulo);
    }

    private void exibirImagemSelecionada(BufferedImage imagem, String titulo) {
        JInternalFrame frame = new JInternalFrame(titulo, true, true, true, true);
        ImagePanel panel = new ImagePanel(imagem);
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.pack();
        theDesktop.add(frame);
        frame.setVisible(true);
    }

    public int[][] obterMatrizVermelha(){
        return matR;
    }

    public int[][] obterMatrizVerde(){
        return matG;
    }

    public int[][] obterMatrizAzul(){
        return matB;
    }

    private static int[] getPixelData(BufferedImage img, int x, int y) {
        int argb = img.getRGB(x, y);

        int rgb[] = new int[] {
                (argb >> 16) & 0xff, //red
                (argb >>  8) & 0xff, //green
                (argb      ) & 0xff  //blue
        };

        return rgb;
    }

    class MyJPanel extends JPanel{
        private ImageIcon imageIcon;

        public MyJPanel(){
            imageIcon = new ImageIcon(path);
        }

        public void setImageIcon(ImageIcon i){
            imageIcon = i;
        }

        public void paintComponent(Graphics g){
            super.paintComponents(g);
            imageIcon.paintIcon(this, g, 0, 0);
        }

        public Dimension getPreferredSize(){
            return new Dimension(imageIcon.getIconWidth(),
                    imageIcon.getIconHeight());
        }

    }

    static class ImagePanel extends JPanel {

        private final BufferedImage image;

        public ImagePanel(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return image != null ? new Dimension(image.getWidth(), image.getHeight()) : super.getPreferredSize();
        }
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

}
