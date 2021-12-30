package HuffmanCoding;
import java.io.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Hamza Akbar
 */

public class HuffmanCoding {
    Node root;
    ArrayList<PixelColor> colors;
    ArrayList<PixelColor> secondaryColorsArr;
    MinHeap mh;
    
    public HuffmanCoding(){
        this.colors = new ArrayList<PixelColor>();
        secondaryColorsArr = new ArrayList<PixelColor>();
        root = null;
    }
    
    public void getColorsFromImage(String imageFile){
        File file = new File(imageFile);
        try{
            FileWriter writer = new FileWriter("pixel_values.txt");
            BufferedImage img = ImageIO.read(file);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int pixel = img.getRGB(x,y);
                    Color color = new Color(pixel, true);
                    int red = color.getRed();
                    int green = color.getGreen();
                    int blue = color.getBlue();
                    int alpha = color.getAlpha(); 
                    String hexa = toHexa(red, green, blue, alpha);
                    String binary = "" + toBinary(red) + toBinary(green) + toBinary(blue) + toBinary(alpha);
                    int frequency = 1;
                    
                    secondaryColorsArr.add(new PixelColor(red, green, blue,alpha, binary, hexa));
                    for(int z = 0; z < colors.size(); z++){
                        if(colors.get(z).hexa.equals(hexa)){
                            frequency += colors.get(z).getFrequency();
                            colors.remove(z);
                            z--;
                        }
                    }
                    colors.add(new PixelColor(red, green, blue, alpha, binary, hexa, frequency));

                    writer.append(binary);
                    writer.flush();
                }
                writer.append("\n");
            }
            mh = new MinHeap(colors.size()); // Creating MinHeap with size of Colors ArrayList
            
            quickSort(colors, 0, colors.size() - 1);
            writer.close();
            for(int i = 0; i < colors.size(); i++){
                mh.insert(new Node(colors.get(i)));
            }
            mh.buildHeap(); // Building MinHeap
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    public String toHexa(int red, int green, int blue, int alpha){
        String hexa = "#";
        hexa += Integer.toHexString(red);
        hexa += Integer.toHexString(green);
        hexa += Integer.toHexString(blue);
        hexa += Integer.toHexString(alpha);
        return hexa;
    }
    
    public String toBinary(int num){
        String bits = "";
        for(; num > 0; num = num / 2){
            bits = num % 2 + bits;
        }
        for(int i = 8 - bits.length(); i > 0; i--){
            bits = "0" +  bits;
        }
        return bits;
    }
  
    public void quickSort(ArrayList<PixelColor> list, int low, int high){
        if(low < high){
            int partitionIndex = partition(low, high, list);
            quickSort(list, low, partitionIndex - 1);
            quickSort(list, partitionIndex + 1, high);
        }
    }
    
    public void swap(int index1, int index2, ArrayList<PixelColor> param){
        if(index1 < param.size() && index2 < param.size()){
            PixelColor temp = param.get(index1);
            param.set(index1, param.get(index2));
            param.set(index2, temp);
        }
    }
    
    public int partition(int low, int high, ArrayList<PixelColor> param){
        PixelColor pivot = param.get(low + ((high - low)/ 2));
        swap(low + ((high - low)/ 2), high, param);
        int i = low - 1; int j = low;
        for(; j < high; j++){
            if(param.get(j).getFrequency() < pivot.getFrequency()){
                swap(++i, j, param);
            }
        }
        swap(++i, high, param);
        return i;
    }
    
    public Node formHuffmanTree(){
        Node dummyRoot = null;
        while(mh.arr[1] != null){
            Node minOne = mh.deleteRoot();
            Node minTwo = mh.deleteRoot();
            minOne.setCode(0);
            minTwo.setCode(1);
            dummyRoot = new Node(minOne.getColor().getFrequency() + minTwo.getColor().getFrequency());
            dummyRoot.left = minOne;
            dummyRoot.right = minTwo;
            mh.insert(dummyRoot);
            mh.heapify();
        }
        return mh.arr[0];
    }
    
    public void encode(Node root, String bits){
        if(root != null){
            encode(root.left, "" + bits + "" + root.getCode());
            if(root.left == null && root.right == null){
                root.getColor().setNewBits(("" + bits + "" + root.getCode()).substring(2));
                System.out.println("Encoded Bits of " + root.getColor().getHexa() + " " + root.getColor().frequency +  ": " + root.getColor().getNewBits());
            }
            encode(root.right, bits + "" + root.getCode());
        }
    }
    
    public void setEncoding(){
        for(int i = 0; i < colors.size(); i++){
            for(int j = 0; j < secondaryColorsArr.size(); j++){
                if(colors.get(i).getHexa().equals(secondaryColorsArr.get(j).getHexa())){
                    secondaryColorsArr.get(j).setNewBits(colors.get(i).getNewBits());
                }
            }
        }
    }
    
    public void writeEncodedDataToFile(String imageFile){
        File file = new File(imageFile);
        try{
            FileWriter writer = new FileWriter("encoded_pixel_values.txt");
            BufferedImage img = ImageIO.read(file);
            int count = 0;
            for(int i = 0; i < img.getHeight(); i++){
                for(int j = 0; j < img.getWidth(); j++){
                    writer.append(secondaryColorsArr.get(i * img.getWidth() + j).getNewBits());
                    writer.flush();
                    count = i * img.getWidth() + j;
                }
                writer.append("\n");
            } 
            writer.close();
        }
        catch(Exception e){
            System.out.println(e);
        }  
    }

    public void generateImage(String imageFile){
        File file = new File(imageFile);
        File newImageFile = new File("regenerated_image.png");
        try{
            BufferedImage img = ImageIO.read(file);
            BufferedImage regenImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0, z = 0; y < img.getHeight(); y++)
            {
                for (int x = 0; x < img.getWidth(); x++)
                {
                    String bits = secondaryColorsArr.get(z++).getOldBits();
                    int red = Integer.parseInt(bits.substring(0, 8), 2);
                    int green = Integer.parseInt(bits.substring(8, 16), 2);
                    int blue = Integer.parseInt(bits.substring(16, 24), 2);
                    int alpha = Integer.parseInt(bits.substring(24, 32), 2);
                    int pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    regenImage.setRGB(x, y, pixel);
                }
            }
            ImageIO.write(regenImage, "png", newImageFile);
        }
        catch(Exception e){
            System.out.println(e);
        }  
    }
    
    public void compress(String fileName, String imageFile){
        try{
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            FileWriter writer = new FileWriter("compressed.txt");
            while(myReader.hasNextLine()) {
                String data = myReader.nextLine();
                for(int i = 0; i < data.length(); i += 8){
                    if(data.substring(i).length() < 8){
                        writer.append((char) (Integer.parseInt(data.substring(i), 2)));
                    }
                    else{
                        writer.append((char) (Integer.parseInt(data.substring(i, i + 8), 2)));
                    }
                }
                writer.append("\n");
            }
            myReader.close();
            writer.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    public void decompress(String fileName, String imageFile){
        File file = new File(imageFile);
        File newImageFile = new File("regenerated_image.png");
        try {
            BufferedImage img = ImageIO.read(file);
            BufferedImage regenImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            newImageFile.getTotalSpace();
            int y = 0, x = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                Node ptr = this.root;
                for(int i = 0; i < data.length(); i++){
                    String bits = Integer.toBinaryString((int) data.charAt(i)); 
                    if(bits.length() < 8){
                        if(bits.length() == 7){
                            if((bits.charAt(0) + "").equals("1")){
                                bits = "0" + bits;
                            }
                            else if((bits.charAt(bits.length() - 1) + "").equals("1")){
                                bits = bits + "0";
                            }
                        }
                        else if(bits.equals("0")){
                            bits = "00000000";
                        }
                    }
                    for(int j = 0; j < bits.length(); j++){
                        if(ptr.left == null && ptr.right == null){
                            int rgba[] = ptr.getColor().getRgba();
                            int pixel = (rgba[3] << 24) | (rgba[0] << 16) | (rgba[1] << 8) | rgba[2];
                            regenImage.setRGB(x++, y, pixel); // Comment this
                            ptr = this.root;
                        }
                        else if(bits.charAt(j) == '0'){
                            ptr = ptr.left;
                        }
                        else if(bits.charAt(j) == '1'){
                            ptr = ptr.right;
                        }
                    }
                }
                ++y;
                x = 0;
            }
            myReader.close();
            generateImage(imageFile);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public void controller(String imageFile){
        getColorsFromImage(imageFile);
        root = formHuffmanTree();
        encode(root, "");
        setEncoding();
        writeEncodedDataToFile(imageFile);
        compress("encoded_pixel_values.txt", imageFile);
        decompress("compressed.txt", imageFile);
    }
    
    
    
}

