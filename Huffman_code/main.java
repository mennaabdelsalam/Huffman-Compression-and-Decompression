package node;
import javax.print.DocFlavor;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;


// A Tree node
class Node
{
    String ch;
    int freq;
    Node left = null, right = null;

    Node(String string, int freq)
    {
        this.ch = string;
        this.freq = freq;
    }
    //
    public Node(String ch, int freq, Node left, Node right) {
        this.ch = ch;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }
};

class Huffman
{
    public static String decompressedstring="";
    // traverse the Huffman Tree and store Huffman Codes
    // in a map.
    public static void encode(Node root, String str,
                              Map<String, String> huffmanCode) //to trasferm the tree to
    {
        if (root == null)
            return;

        // found a leaf node
        if (root.left == null && root.right == null) {
            huffmanCode.put(root.ch, str);
        }
        //recursive call for encode to cheack the availability of 0 & 1 to encode them
        encode(root.left, str + "0", huffmanCode);
        encode(root.right, str + "1", huffmanCode);

    }


    public static int decode(Node root, int index, String sb)
    {
        if (root == null)
            return index;

        // found a leaf node
        if (root.left == null && root.right == null)
        {

            decompressedstring=decompressedstring+root.ch;
            return index;
        }


        index++;

        if (sb.charAt(index) == '0')
            index = decode(root.left, index, sb);
        else
            index = decode(root.right, index, sb);

        return index;
    }

    public static void com(String file, int n, String out_file) throws IOException
    {
        //n= 5;
        long startTime = System.currentTimeMillis();
        // String out_file = "6538."+ n+ "."+ file;
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        //The Java FileInputStream class, java.io.FileInputStream, makes it possible to read the contents of a file as a stream of bytes.

        int byteRead;
        int z= 0;
        String bytes = "";

        Map<String, Integer> freq = new HashMap<>();
        while ((byteRead = inputStream.read()) != -1) {
            bytes += byteRead + ",";
            z+=1;
            if(z == n) {
                if (!freq.containsKey(bytes)) {
                    freq.put(bytes, 0);

                }
                freq.put(bytes, freq.get(bytes) + 1);
                bytes = "";
                z=0;
            }
        }
        bytes += "~";

        inputStream.close();




        PriorityQueue<Node> pq = new PriorityQueue<>(
                (l, r) -> l.freq - r.freq);


        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (pq.size() != 1)
        {
            Node left = pq.poll();
            Node right = pq.poll();

            int sum = left.freq + right.freq;
            pq.add(new Node("", sum, left, right));
        }

        Node root = pq.peek();

        Map<String, String> huffmanCode = new HashMap<>();
        encode(root, "", huffmanCode);



        int binary_size = 0;
        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            binary_size += entry.getValue()*huffmanCode.get(entry.getKey()).length();
        }


        int add = 8-binary_size%8;
        if(add==8)
            add = 0;
        binary_size += add;



        Path path = Paths.get(out_file);
        Files.deleteIfExists(path);
        Files.createFile(path);
        String Ff="";
        StringBuilder sb = new StringBuilder();
        sb.append(n+"~");
        Files.write(path, bytes.getBytes(), StandardOpenOption.APPEND);

        for (Map.Entry<String, Integer> entry : freq.entrySet())
        {
            sb.append(entry.getKey());
            sb.append(entry.getValue()+"~");


        }
        Ff = sb.toString() + " " + binary_size/8 + " " + add + " ";

        byte[] info = Ff.getBytes();


        Files.write(path, info, StandardOpenOption.APPEND);
        // put data in buffer
        // once buffer full. put in file and clear buffer
        // so we dont run out of memory
        byte[] buff = new byte[1000];
        inputStream = new BufferedInputStream(new FileInputStream(file));
        int b = 0; //current position of the buffer
        String code = "";
        z = 0;
        bytes = "";
        while ((byteRead = inputStream.read()) != -1) {

            bytes += byteRead + ",";
            z+=1;
            if(z == n) {
                code += huffmanCode.get(bytes);
                while(code.length()>=8) {
                    String binaryString = code.substring(0,8);
                    byte thisbyte = (byte)Integer.parseInt(binaryString, 2);
                    buff[b++] = thisbyte;
                    if(b == 1000) {
                        b = 0;
                        Files.write(path, buff, StandardOpenOption.APPEND);
                    }
                    code = code.substring(8,code.length());
                }

                z=0;
                bytes ="";
            }

        }

        if(add != 0) {
            for(int j = 0; j<add;j++)
                code = code + '0';
            buff[b++] = (byte)Integer.parseInt(code, 2);
        }



        Files.write(path, Arrays.copyOfRange(buff, 0, b), StandardOpenOption.APPEND);

        inputStream.close();

        long endTime = System.currentTimeMillis();

        System.out.println("Compression took " + (endTime - startTime)/1000 + " seconds");


    }

    public static void decom(String file, String extracted) throws IOException {

        long startTime = System.currentTimeMillis();


        File filename = new File(file);
        //String extracted="";
        // extracted="extracted."+filename.getName();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        int byteRead;
        int delimiter = (int) '~';
        int delimiter2 = (int) ',';
        Map<String, Integer> freq = new HashMap<>();

        // first get tail bytes
        Vector<Integer> tail_bytes=new Vector<Integer>(0);
        while ((byteRead = inputStream.read()) != delimiter) {
            String byteVal2 = "";
            while(byteRead != delimiter2){
                byteVal2 += String.valueOf((char)byteRead);
                byteRead = inputStream.read();
            }

            tail_bytes.add(Integer.parseInt(byteVal2));

        }

        int size2 = tail_bytes.size();
        byte tailBytes[] = new byte[size2];

        for(int c=0;c<size2;c++) {
            tailBytes[c] = (byte)(int)tail_bytes.get(c);
        }


        // first get n
        int n;
        String byteVal = "";
        while ((byteRead = inputStream.read()) != delimiter) {
            byteVal  += (char) byteRead;
        }

        n = Integer.parseInt(byteVal) ;


        // next read bytes
        String byteFreq;
        String bytes;
        while ((byteRead = inputStream.read()) != 32) {
            byteVal = "";
            byteFreq = "";
            bytes ="";
            for(int k =0;k<n;k++) {
                while(byteRead != delimiter2){

                    byteVal += String.valueOf((char)byteRead);
                    byteRead = inputStream.read();
                }
                byteRead = inputStream.read();
                bytes += byteVal + ",";
                byteVal = "";

            }

            while(byteRead != delimiter){
                byteFreq += (char) byteRead;
                byteRead = inputStream.read();
            }



            freq.put(bytes,Integer.parseInt(byteFreq));



        }



        PriorityQueue<Node> pq = new PriorityQueue<>(
                (l, r) -> l.freq - r.freq);

        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            pq.add(new Node(entry.getKey(), entry.getValue()));
        }
        while (pq.size() != 1) {
            Node left = pq.poll();
            Node right = pq.poll();

            int sum = left.freq + right.freq;
            pq.add(new Node("", sum, left, right));
        }

        Node root = pq.peek();

        Map<String, String> huffmanCode = new HashMap<>();
        encode(root, "", huffmanCode);

        Map<String, String> inversehuffmanCode = new HashMap<>();


        for (Map.Entry<String, String> entry : huffmanCode.entrySet()) {

            inversehuffmanCode.put(entry.getValue(),entry.getKey());
        }


        String num_bytes_str = "";
        int numbytes = 0;


        while ((byteRead = inputStream.read()) != 32) {
            num_bytes_str += (char) byteRead;
        }


        numbytes = Integer.parseInt(num_bytes_str);
        byteRead = inputStream.read();
        int padding = (int) byteRead - 48;
        byteRead = inputStream.read();

        FileWriter fg = new FileWriter(extracted);
        Path path = Paths.get(extracted);
        String code = "";
        int i=0;
        int j =1;
        // we will have a 1000 byte buffer
        // one full we write in file and clear
        // so we dont run out of memory
        int buff_size = 1000000000;
        byte[] buff = new byte[buff_size];
        int b =0;s
        while(i < numbytes) {
            i++;
            byteRead = inputStream.read();
            String thisByte = String.format("%8s", Integer.toBinaryString(byteRead)).replace(' ','0');
            if(i==numbytes) // last byte remove last padding beats
                thisByte = thisByte.substring(0,thisByte.length()-padding);
            code += thisByte;
            for(j=j;j<=code.length();j++) {
                String sub = code.substring(0,j);

                if(inversehuffmanCode.containsKey(sub)) {
                    String[] tokens=inversehuffmanCode.get(sub).split(",");
                    int size = tokens.length;
                    for(int c=0; c<size; c++) {
                        buff[b++] += (byte) (int) Integer.parseInt(tokens[c]);
                        if(b == buff_size) {

                            b = 0;
                            Files.write(path, buff, StandardOpenOption.APPEND);
                        }
                    }


                    code = code.substring(j,code.length());
                    j = 0;
                }

            }
        }

        // in case buffer still has data

        Files.write(path, Arrays.copyOfRange(buff, 0, b), StandardOpenOption.APPEND);
        Files.write(path, Arrays.copyOfRange(tailBytes, 0, size2), StandardOpenOption.APPEND);

        inputStream.close();
        fg.close();

        long endTime = System.currentTimeMillis();

        System.out.println("Decompression took " + (endTime - startTime)/1000 + " seconds");
    }



    public static void compression (String directory,int n){  //reading + output

        byte[] fileContent = null;
        File file = new File(directory);
        try {
            fileContent= Files.readAllBytes(file.toPath());//-->>file.topath should be an array
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("file: " + fileContent.length);
        byte[] fileContent2 = null;
        File filename = new File(directory);
        //System.out.println(filename.getName());
        String H="";
        H="6538."+n+"."+filename.getName();
        File file2 = new File(H);
        try {
            fileContent2= Files.readAllBytes(file2.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("file2: "+fileContent2.length);
        float compressionratio=(float)fileContent2.length/fileContent.length;
        System.out.println("Compression ratio = "+(int)Math.ceil(compressionratio*100)+"%");

    }
    public static void print (String directory) throws IOException{

        BufferedWriter writer = new BufferedWriter(new FileWriter(directory));
        writer.write(decompressedstring);
        writer.close();

    }


    public static void main(String[] args) throws IOException {
        String op = args[0];
        String in_file = args[1];
        File temp = new File(in_file);
        String path  = temp.getParent();
        String name  = temp.getName();

        if(op.contains("c")) {
            int n = Integer.parseInt(args[2]);
            String out_file = path + "\\6538.n." + name;
            com(in_file,n,out_file);
        }

        if(op.contains("d")) {
            String out_file = path + "\\extracted." + name;
            decom(in_file,out_file);
        }




    }
}