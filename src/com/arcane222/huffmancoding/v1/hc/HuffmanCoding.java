/**
 * @class HuffmanCoding
 * @author Lee Hong Jun (arcane22, hong3883@naver.com)
 * @description - Last modified 2021. 12. 09
 * - encoding(), decoding() 메소드를 통하여 txt 파일을 인코딩, 디코딩함.
 * - huffmanCoding().encoding(file) .txt 압축(인코딩) -> .bin 생성
 * - huffmanCoding().decoding(file) 생성된 .bin 디코딩 -> .txt 생성
 */

package com.arcane222.huffmancoding.v1.hc;

import java.io.*;
import java.util.*;


public class HuffmanCoding {

    private static final int BYTE_SIZE = 8;

    /*** Instance Variable ***/
    private Node root;

    private PriorityQueue<Node> pq;
    private HashMap<Character, Node> map;
    private LinkedList<Node> leafList;

    /*** Constructor ***/
    public HuffmanCoding() {
    }

    /**
     * node type 의 list 에 parameter 의 character 를 가지고있는 노드가 존재하는 지 탐색
     * 노드가 존재할 경우 해당 node의 index 값 return , 존재하지 않는다면 -1 return
     *
     * @param c
     * @param list
     * @return
     */
    @Deprecated
    public int containsChar(char c, LinkedList<Node> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getElement() == c)
                return i;
        }
        return -1;
    }

    /**
     * 빈도수를 기준으로 하여 node type의 list를 정렬함
     * Comparator 의 compare 메소드를 override 하고 있는데 n1 - n2 하면 오름차순, n2 - n1 하면 내림차순으로 정렬됨
     *
     * @param list
     * @Deprecated
     */
    @Deprecated
    public void sortListByFrequency(LinkedList<Node> list) {
        list.sort(Comparator.comparingInt(Node::getFrequency));
    }

    /**
     * buffer string 의 각 문자의 빈도수를 계산하여 node 에 저장함.
     *
     * @param buffer
     */
    public void calcuCharFrequency(StringBuilder buffer) {
        for (int i = 0; i < buffer.length(); i++) {
            char c = buffer.charAt(i);
            if (map.containsKey(c))
                map.get(c).setFrequency(map.get(c).getFrequency() + 1);
            else
                map.put(c, new Node(c, 1));
        }

        pq.addAll(map.values());
    }

    /**
     * nodeList 를 가지고 허프만 트리를 만듬. nodeList 에는 빈도수가 계산된 character 를 담고있는 node 들이 존재함.
     */
    public void makeHuffmanTree() {
        if (pq.size() == 1) {
            Node leftChild = pq.peek();
            root = new Node('\0', leftChild.getFrequency());
            leftChild.setParent(root);
            leftChild.setBitValue(0);
            root.setLeftChild(leftChild);
        } else {
            while (pq.size() > 1) {
                Node leftNode = pq.poll();
                Node rightNode = pq.poll();
                assert rightNode != null;
                root = new Node('\0', leftNode.getFrequency() + rightNode.getFrequency());

                leftNode.setParent(root);
                leftNode.setBitValue(0);
                rightNode.setParent(root);
                rightNode.setBitValue(1);

                root.setLeftChild(leftNode);
                root.setRightChild(rightNode);

                pq.add(root);
            }
        }
    }

    /**
     * 트리의 root 부터 노드들을 탐색하여 leaf 노드를 찾아내고 이것으로 leaf node list 를 만듬.
     */
    public void makeLeafList() {
        ArrayList<Node> queue = new ArrayList<>();
        queue.add(root);
        while (queue.size() > 0) {
            Node curr = queue.remove(0);
            Node left = curr.getLeftChild();
            Node right = curr.getRightChild();

            if (left != null)
                queue.add(left);

            if (right != null)
                queue.add(right);

            if (curr.isLeaf())
                leafList.add(curr);
        }
    }

    /**
     * leaf list 를 가지고 각 글자의 허프만 코드를 만들어서 할당하고 이를 이용하여 입력받은 buffer string 을 인코딩함.
     *
     * @param buffer
     * @return
     */
    public StringBuilder makeHuffmanCode(StringBuilder buffer) {
        // 각 leaf 노드의 parent 를 탐색하며 글자별 허프만 코드를 만들어 할당함
        for (int i = 0; i < leafList.size(); i++) {
            StringBuilder codeBuffer = new StringBuilder();
            Node leafNode = leafList.get(i);
            while (leafNode.getParent() != null) {
                codeBuffer.append(leafNode.getBitValue());
                leafNode = leafNode.getParent();
            }

            codeBuffer.reverse();
            leafList.get(i).setHuffmanCode(codeBuffer.toString());
        }

        // leafList 에 있는 글자 node 들의 huffman code 를 가지고 buffer string 의 허프만 코드를 만듬.
        StringBuilder encodingBuffer = new StringBuilder();
        for (int i = 0; i < buffer.length(); i++) {
            char c = buffer.charAt(i);
            int idx = containsChar(c, leafList);
            encodingBuffer.append(leafList.get(idx).getHuffmanCode());
        }
        return encodingBuffer;
    }

    /**
     * 00011 과 같은 binary string 에 대하여 integer 로 읽을 경우 11로 읽히게 된다.
     * 이럴 경우 앞에 손실된 0값을 채워주는 역할을 하는 메소드
     *
     * @param binaryString
     * @param fullSize
     * @return
     */
    public String zeroPadding(String binaryString, int fullSize) {
        int length = binaryString.length();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < fullSize - length; i++)
            buffer.append(0);
        buffer.append(binaryString);

        return buffer.toString();
    }


    /**
     * .txt 파일을 읽어 .bin 의 압축파일을 만들어주는 인코딩 메소드
     *
     * @param inputFile
     */
    public void encoding(File inputFile) {
        StringBuilder fileName = new StringBuilder("./input/" + inputFile.getName().split("\\.")[0]).append(".bin");
        StringBuilder fileStrBuffer = new StringBuilder();
        StringBuilder encodingBuffer;

        File outputFile = new File(String.valueOf(fileName));
        map = new HashMap<>();
        pq = new PriorityQueue<>();
        leafList = new LinkedList<>();

        try (FileInputStream fis = new FileInputStream(inputFile);
             DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
             FileOutputStream fos = new FileOutputStream(outputFile);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos))) {

            // 1바이트씩 읽어와 fileStrBuffer 에 저장함.
            while (true) {
                int byteVal = dis.read();
                if (byteVal != -1) // 만약 파일의 끝(EOF)라면 -1 .read() 메소드는 -1의 값을 리턴함.
                    fileStrBuffer.append((char) byteVal);
                else break;
            }

            // 문자열 빈도수 계산
            calcuCharFrequency(fileStrBuffer);

            // 허프만 트리 구축
            makeHuffmanTree();

            // 잎 노드 목록 구축
            makeLeafList();

            // 문자열 인코딩
            encodingBuffer = makeHuffmanCode(fileStrBuffer);


            // 허프만 트리정보 및 허프만 코드 전체의 길이를 파일에 입력함.
            // (char 의 종류의 수) / (char) (허프만코드 길이) (실제 코드를 integer 로 변환, 크기에 맞게 byte, short, int로 씀) / ... 반복 / (전체 허프만코드 길이) / (전체 허프만코드)
            dos.write(leafList.size());
            for (int i = 0; i < leafList.size(); i++) {
                int length = leafList.get(i).getHuffmanCode().length();
                int codeVal = Integer.parseInt(leafList.get(i).getHuffmanCode(), 2);
                dos.write(leafList.get(i).getElement());
                dos.write(length);
                if (length <= BYTE_SIZE)
                    dos.write(codeVal);
                else if (length <= (BYTE_SIZE << 1))
                    dos.writeShort(codeVal);
                else
                    dos.writeInt(codeVal);
            }


            dos.writeInt(encodingBuffer.length()); //전체 허프만코드 길이
            // 전체 허프만 코드를 출력
            int sp = 0; // src pointer
            while (encodingBuffer.length() > sp) {
                int codeVal = 0;
                if (encodingBuffer.length() - sp > BYTE_SIZE)
                    codeVal = Integer.parseInt(encodingBuffer.substring(sp, sp + BYTE_SIZE), 2);
                else
                    codeVal = Integer.parseInt(encodingBuffer.substring(sp, encodingBuffer.length()), 2);

                dos.write(codeVal);
                sp += BYTE_SIZE;
            }

            System.out.println("인코딩이 완료되었습니다.");
            resultDump(inputFile, outputFile);
            System.out.printf("압축률: %f (original / compressed)%n", inputFile.length() / (float) outputFile.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * .bin 파일을 읽어 다시 .txt 로 원복시켜주는 디코딩 메소드
     *
     * @param inputFile
     */
    public void decoding(File inputFile) {
        HashMap<String, Character> decodingTable = new HashMap<>();
        StringBuilder decodingBuffer = new StringBuilder();
        File outputFile = new File("./output/output.txt");
        int codeFullLength = 0;

        try (FileInputStream fis = new FileInputStream(inputFile);
             DataInputStream dis = new DataInputStream(new BufferedInputStream(fis));
             FileOutputStream fos = new FileOutputStream(outputFile);
             DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(fos))) {

            // encoding 시 입력했던 방식대로 다시 읽어와서 디코딩을 실시한다.
            int charCount = dis.read();
            for (int i = 0; i < charCount; i++) {
                char c = (char) dis.read();
                int length = dis.read();
                String code;
                if (length <= BYTE_SIZE)
                    code = Integer.toBinaryString(dis.read());
                else if (length <= (BYTE_SIZE << 1))
                    code = Integer.toBinaryString(dis.readShort());
                else
                    code = Integer.toBinaryString(dis.readInt());
                code = zeroPadding(code, length);

                decodingTable.put(code, c);
            }

            // 허프만 코드 전체의 길이를 읽고 그 뒤의 전체 허프만 코드를 읽어오는데, 8비트씩 끊어서 읽어온다
            // 원 코드가 01110000일 경우, 1110000 처럼 읽히므로 모자란부분을 0으로채워줌
            codeFullLength = dis.readInt();
            for (int i = 0; i < (codeFullLength >> 3); i++) {
                int val = dis.read();
                String buffer = zeroPadding(Integer.toBinaryString(val), BYTE_SIZE);
                decodingBuffer.append(buffer);
            }

            if (codeFullLength % BYTE_SIZE != 0) //
                //마지막 8비트는 비트수 만큼만 0을 채워줌 ex) 마지막 코드가 00011일 경우 11로 읽혀 오므로 실제 길이 5 - 읽힌 길이2 = 3만큼 0을 채워줌
                decodingBuffer.append(zeroPadding(Integer.toBinaryString(dis.read()), codeFullLength % BYTE_SIZE));


            // 원복한 전체 허프만 코드가 들어있는 decodingBuffer 의 값을 읽어와 허프만 코드와 문자의 정보가 포함된 decodingTable 을 이용하여
            // 디코딩을 실시하고 원래파일로 원복시킨다.
            int sp = 0; // src pointer
            String buffer = "";
            while (decodingBuffer.length() > sp) {
                buffer += decodingBuffer.charAt(sp++);

                if (decodingTable.containsKey(buffer)) {
                    dos.write(decodingTable.get(buffer));
                    buffer = "";
                }
            }

            System.out.println("디코딩이 완료되었습니다.");
            resultDump(inputFile, outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * inputFile, outputFile의 정보를 dump
     *
     * @param inputFile
     * @param outputFile
     */
    public void resultDump(File inputFile, File outputFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("입력파일: ");
        sb.append(inputFile.getName());
        sb.append(", ");
        sb.append(inputFile.length());
        sb.append(" (bytes)");
        System.out.println(sb);

        sb.delete(0, sb.length());
        sb.append("출력파일: ");
        sb.append(outputFile.getName());
        sb.append(", ");
        sb.append(outputFile.length());
        sb.append(" (bytes)");
        System.out.println(sb);
    }

    public void resultDump(RandomAccessFile inputFile, RandomAccessFile outputFile) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("입력파일: ");
        sb.append(inputFile.toString());
        sb.append(", ");
        sb.append(inputFile.length());
        sb.append(" (bytes)");
        System.out.println(sb);

        sb.delete(0, sb.length());
        sb.append("출력파일: ");
        sb.append(outputFile.toString());
        sb.append(", ");
        sb.append(outputFile.length());
        sb.append(" (bytes)");
        System.out.println(sb);
    }
}
