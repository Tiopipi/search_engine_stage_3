package org.example.utils;

import org.example.model.InvertedIndex;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinarySerializer implements Serializer {

    @Override
    public void write(Map<String, InvertedIndex> data, String filePath) throws IOException {
        Map<String, BinaryInvertedIndex> serializableData = new HashMap<>();
        data.forEach((key, value) -> serializableData.put(key, new BinaryInvertedIndex(value)));

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(serializableData);
        } catch (IOException e) {
            throw new IOException("Error writing binary data to file: " + filePath, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, InvertedIndex> read(String filePath) throws IOException {
        File binaryFile = new File(filePath);
        if (!binaryFile.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(binaryFile)))) {
            Map<String, BinaryInvertedIndex> serializableData =
                    (Map<String, BinaryInvertedIndex>) ois.readObject();

            Map<String, InvertedIndex> data = new HashMap<>();
            serializableData.forEach((key, value) -> data.put(key, value.toInvertedIndex()));
            return data;
        } catch (ClassNotFoundException | IOException e) {
            throw new IOException("Error reading binary data from file: " + filePath, e);
        }
    }

    private static class BinaryInvertedIndex implements Externalizable {
        private static final long serialVersionUID = 1L;

        private List<String> id;
        private List<List<Integer>> positions;
        private List<Integer> frequencies;

        // Default constructor for deserialization
        public BinaryInvertedIndex() {}

        // Constructor for direct conversion
        public BinaryInvertedIndex(InvertedIndex base) {
            this.id = base.getDocIds();
            this.positions = base.getPositions();
            this.frequencies = base.getFrequencies();
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            // Serialize the list of document IDs
            out.writeInt(id.size());
            for (String docId : id) {
                out.writeUTF(docId);
            }

            // Serialize the list of positions
            out.writeInt(positions.size());
            for (List<Integer> positionList : positions) {
                out.writeInt(positionList.size());
                for (int pos : positionList) {
                    out.writeInt(pos);
                }
            }

            // Serialize the frequencies
            out.writeInt(frequencies.size());
            for (int freq : frequencies) {
                out.writeInt(freq);
            }
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException {
            // Deserialize the list of document IDs
            int idSize = in.readInt();
            id = new java.util.ArrayList<>(idSize);
            for (int i = 0; i < idSize; i++) {
                id.add(in.readUTF());
            }

            // Deserialize the list of positions
            int positionsSize = in.readInt();
            positions = new java.util.ArrayList<>(positionsSize);
            for (int i = 0; i < positionsSize; i++) {
                int positionListSize = in.readInt();
                List<Integer> positionList = new java.util.ArrayList<>(positionListSize);
                for (int j = 0; j < positionListSize; j++) {
                    positionList.add(in.readInt());
                }
                positions.add(positionList);
            }

            // Deserialize the frequencies
            int frequenciesSize = in.readInt();
            frequencies = new java.util.ArrayList<>(frequenciesSize);
            for (int i = 0; i < frequenciesSize; i++) {
                frequencies.add(in.readInt());
            }
        }

        public InvertedIndex toInvertedIndex() {
            InvertedIndex base = new InvertedIndex();
            base.setDocIds(this.id);
            base.setPositions(this.positions);
            base.setFrequencies(this.frequencies);
            return base;
        }
    }
}