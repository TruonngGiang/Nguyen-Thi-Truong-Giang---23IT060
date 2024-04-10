package BaiThiGiuaKi2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateStudentXML {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            File inputFile = new File("student.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList studentList = doc.getElementsByTagName("student");

            for (int i = 0; i < studentList.getLength(); i++) {
                Element studentElement = (Element) studentList.item(i);

                String id = studentElement.getElementsByTagName("id").item(0).getTextContent();
                String name = studentElement.getElementsByTagName("name").item(0).getTextContent();
                String address = studentElement.getElementsByTagName("address").item(0).getTextContent();
                String dateOfBirth = studentElement.getElementsByTagName("dateOfBirth").item(0).getTextContent();

                executor.submit(() -> {
                    int age = calculateAge(dateOfBirth);
                    String encodedAge = encodeDigits(age);
                    boolean isPrime = isPrimeNumber(sumOfDigits(dateOfBirth));

                    writeStudentToXml(id, name, address, dateOfBirth);
                });
            }

            executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int calculateAge(String dateOfBirth) {
        LocalDate birthDate = LocalDate.parse(dateOfBirth);
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(birthDate, currentDate);
        return period.getYears();
    }

    private static String encodeDigits(int number) {
        String numberStr = String.valueOf(number);
        StringBuilder encoded = new StringBuilder();
        for (int i = numberStr.length() - 1; i >= 0; i--) {
            encoded.append(numberStr.charAt(i));
        }
        return encoded.toString();
    }

    private static int sumOfDigits(String dateOfBirth) {
        int sum = 0;
        for (char c : dateOfBirth.toCharArray()) {
            if (Character.isDigit(c)) {
                sum += Character.getNumericValue(c);
            }
        }
        return sum;
    }

    private static boolean isPrimeNumber(int number) {
        if (number <= 1) {
            return false;
        }
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    private static void writeStudentToXml(String id, String name, String address, String dateOfBirth) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element rootElement = doc.createElement("students");
            doc.appendChild(rootElement);

            Element studentElement = doc.createElement("student");
            rootElement.appendChild(studentElement);

            Element idElement = doc.createElement("id");
            idElement.appendChild(doc.createTextNode(id));
            studentElement.appendChild(idElement);

            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(name));
            studentElement.appendChild(nameElement);

            Element addressElement = doc.createElement("address");
            addressElement.appendChild(doc.createTextNode(address));
            studentElement.appendChild(addressElement);

            Element dobElement = doc.createElement("dateOfBirth");
            dobElement.appendChild(doc.createTextNode(dateOfBirth));
            studentElement.appendChild(dobElement);

            // Ghi dữ liệu vào file students.xml
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("students.xml"));
            transformer.transform(source, result);

            System.out.println("File students.xml da duoc tao thanh cong.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
