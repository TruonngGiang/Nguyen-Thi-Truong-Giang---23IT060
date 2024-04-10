package BaiThiGiuaKi2;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentProcessor {

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
                String dateOfBirth = studentElement.getElementsByTagName("dateOfBirth").item(0).getTextContent();

                // Thread 2: Calculate age and encode date of birth
                executor.submit(() -> processStudentData(studentElement, dateOfBirth));

                // Thread 3: Check if sum of digits is prime
                executor.submit(() -> checkPrimeSumOfDigits(dateOfBirth));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Shutdown the executor service after completing all tasks
            executor.shutdown();
        }
    }

    private static void processStudentData(Element studentElement, String dateOfBirth) {
        try {
            LocalDate dob = LocalDate.parse(dateOfBirth);
            LocalDate now = LocalDate.now();
            Period period = Period.between(dob, now);

            int ageYears = period.getYears();
            int ageDays = period.getDays();

            String encodedDateOfBirth = encodeDigits(dateOfBirth);

            writeResultToXML(studentElement, ageYears, encodedDateOfBirth, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void checkPrimeSumOfDigits(String dateOfBirth) {
        try {
            int sumOfDigits = calculateSumOfDigits(dateOfBirth);
            boolean isPrime = isPrimeNumber(sumOfDigits);

            writeResultToXML(null, sumOfDigits, dateOfBirth, isPrime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encodeDigits(String numberStr) {
        StringBuilder encoded = new StringBuilder();

        for (char c : numberStr.toCharArray()) {
            if (Character.isDigit(c)) {
                int digit = Character.getNumericValue(c);
                encoded.append(digit * 2); // Example simple encoding (multiply digit by 2)
            } else {
                encoded.append(c); // Keep non-digit characters as they are
            }
        }

        return encoded.toString();
    }

    private static int calculateSumOfDigits(String numberStr) {
        int sum = 0;

        for (char c : numberStr.toCharArray()) {
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

    private static synchronized void writeResultToXML(Element studentElement, int age, String encodedDateOfBirth, boolean isPrime) {
        try (FileWriter writer = new FileWriter("kq.xml", true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            if (studentElement != null) {
                String id = getElementText(studentElement, "id");
                String name = getElementText(studentElement, "name");
                String address = getElementText(studentElement, "address");

                String xmlString = "<student>\n" +
                        "    <id>" + id + "</id>\n" +
                        "    <name>" + name + "</name>\n" +
                        "    <address>" + address + "</address>\n" +
                        "    <age>" + age + "</age>\n" +
                        "    <encodedDateOfBirth>" + encodedDateOfBirth + "</encodedDateOfBirth>\n" +
                        "</student>\n";

                bufferedWriter.write(xmlString);
            } else {
                String sumOfDigitsXml = "<sumOfDigits>\n   <value>" + age + "</value>\n    <isPrime>" + isPrime + "</isPrime>\n</sumOfDigits>\n";
                bufferedWriter.write(sumOfDigitsXml);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getElementText(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}
