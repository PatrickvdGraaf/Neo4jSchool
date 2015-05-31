package setup;

import enums.Labels;
import enums.Relationship;
import models.Class;
import models.Student;
import models.Subject;
import models.Teacher;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This class is used to...
 * Created by Patrick van de Graaf
 */
public class DatabaseController {
    private GraphDatabaseService graphDb;

    private final String[] studentNames = {"Onno", "Mitchell", "Nick", "Pepijn", "Deborah", "Leonie", "Laura", "Paul", "Wieland", "Marieke", "Ilse", "Steven", "Patrick", "Nav", "Daniël", "Robert", "Boy", "David", "Corné", "Ferry", "Linda", "Patrick", "Edwin", "Yael", "Dirk"};
    private final String[] studentMiddleNames = {"", "", "", "", "", "","", "", "", "", "", "", "", "Nirojan", "", "", "", "", "", "", "", "", "", "", "Pieter"};
    private final String[] studentSurNames = {"van Wingerden", "Herrijgers", "Gobeé", "Stroo", "Gaanderse", "Becker", "van de Graaf", "den Hartigh", "Juch", "Reijm", "Zwaan", "Plaisier", "de Jong", "Appayia", "van der Giessen", "van der Vlies", "Gorissen", "van der Giessen", "van den Berg", "Koster", "de Graaf", "van de Graaf", "Kortman", "Groeneveld", "Jens"};

    private final String[] subjects = {"Development", "Analyse", "Software Engeneering", "SLC", "Skills", "Management", "Data Science"};
    private final String[] subjectCodes = {"INFDEV", "INFANL", "INFSEN", "INFSLC", "INFSKL", "INFMAN", "INFDTA"};

    private final String[] teacherIds = {"JOZIB", "TOOJJ", "KLEIW", "UBERT", "PADAM", "BUSAL", "INGKL", "PARIS"};
    private final String[] teacherNames = {"Bob", "Hans", "Wendy", "Tanja", "Arne", "Tony", "Kevin", "Stelian"};
    private final String[] teacherMiddleNames = {"", "", "", "", "", "", "", ""};
    private final String[] teacherSurnames = {"Joziasse", "van Toor", "Kleij", "Ubert", "Padmos", "Busker", "van Ingen", "Parashiv"};

    private static DatabaseController instance = null;

    protected DatabaseController(){
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();

        graphDb = graphDbFactory.newEmbeddedDatabaseBuilder("data/schoolDb")
                .loadPropertiesFromFile("neo4j.properties")
                .newGraphDatabase();
    }

    public static DatabaseController getInstance() {
        if (instance == null) {
            synchronized (DatabaseController.class) {
                if (instance == null) {
                    instance = new DatabaseController();
                }
            }
        }
        return instance;
    }

    public void executeOperation(String operation){
        Result result = graphDb.execute(operation);
        String dumped = result.resultAsString();
        System.out.println(operation);
        System.out.println(dumped);
    }

    public void setup(){
        try(Transaction tx = graphDb.beginTx()){

            //!!!
            //For now, we remove all data when the setup is executed
            executeOperation("MATCH (n) " +
                    "OPTIONAL MATCH (n)-[r]-()" +
                    "DELETE n,r");

            //We start with 5 classes
            Node classA = graphDb.createNode(Labels.Class);
            classA.setProperty(Class.CLASS_CODE, "INF3A");

            Node classB = graphDb.createNode(Labels.Class);
            classB.setProperty(Class.CLASS_CODE, "INF3B");

            Node classC = graphDb.createNode(Labels.Class);
            classC.setProperty(Class.CLASS_CODE, "INF3C");

            Node classD = graphDb.createNode(Labels.Class);
            classD.setProperty(Class.CLASS_CODE, "INF3D");

            Node classE = graphDb.createNode(Labels.Class);
            classE.setProperty(Class.CLASS_CODE, "INF3E");

            //And the teachers that can mentor the class and will teach subjects
            HashMap<String, Node> teacherNodes = new HashMap<>();
            for (int i = 0; i<teacherIds.length; i++){
                Node teacher = graphDb.createNode(Labels.Teacher);
                teacher.setProperty(Teacher.TEACHER_ID, teacherIds[i]);
                teacher.setProperty(Teacher.NAME, teacherNames[i]);
                teacher.setProperty(Teacher.MIDDLE_NAME, teacherMiddleNames[i]);
                teacher.setProperty(Teacher.SURNAME, teacherSurnames[i]);

                teacherNodes.put(teacherIds[i], teacher);
            }

            Random rand = new Random();
            //Teachers can be friends with each other
            for(Node n : teacherNodes.values()) {
                int numberOfFriends = rand.nextInt(2) + 1;
                for (int i = 0; i < numberOfFriends; i++) {
                    int randomFriend = rand.nextInt(teacherNodes.size());
                    if (teacherNodes.get(teacherIds[randomFriend]) == n) {
                        randomFriend++;
                    }
                    n.createRelationshipTo(teacherNodes.get(teacherIds[randomFriend]), Relationship.IS_FRIENDS_WITH);
                }
            }

            teacherNodes.get("KLEIW").createRelationshipTo(classA, Relationship.MENTORS);
            teacherNodes.get("JOZIB").createRelationshipTo(classB, Relationship.MENTORS);
            teacherNodes.get("INGKL").createRelationshipTo(classC, Relationship.MENTORS);
            teacherNodes.get("PARIS").createRelationshipTo(classD, Relationship.MENTORS);
            teacherNodes.get("UBERT").createRelationshipTo(classE, Relationship.MENTORS);

            //Then all the subjects a student can follow. Some are mandatory
            HashMap<String, Node> subjectNodes = new HashMap<>();
            for(int i = 0; i < subjects.length; i++){
                Node subject = graphDb.createNode(Labels.Subject);
                subject.setProperty(Subject.SUBJECT_CODE, subjectCodes[i]);
                subject.setProperty(Subject.NAME, subjects[i]);
                subjectNodes.put(subjectCodes[i], subject);
            }

            //These subjects must be given by a teacher
            teacherNodes.get("KLEIW").createRelationshipTo(subjectNodes.get("INFSKL"), Relationship.TEACHES);
            teacherNodes.get("UBERT").createRelationshipTo(subjectNodes.get("INFSKL"), Relationship.TEACHES);

            teacherNodes.get("JOZIB").createRelationshipTo(subjectNodes.get("INFDEV"), Relationship.TEACHES);
            teacherNodes.get("TOOJJ").createRelationshipTo(subjectNodes.get("INFDEV"), Relationship.TEACHES);
            teacherNodes.get("INGKL").createRelationshipTo(subjectNodes.get("INFDEV"), Relationship.TEACHES);

            teacherNodes.get("PADAM").createRelationshipTo(subjectNodes.get("INFANL"), Relationship.TEACHES);
            teacherNodes.get("PARIS").createRelationshipTo(subjectNodes.get("INFANL"), Relationship.TEACHES);

            teacherNodes.get("BUSAL").createRelationshipTo(subjectNodes.get("INFSEN"), Relationship.TEACHES);
            teacherNodes.get("TOOJJ").createRelationshipTo(subjectNodes.get("INFSEN"), Relationship.TEACHES);

            teacherNodes.get("KLEIW").createRelationshipTo(subjectNodes.get("INFSLC"), Relationship.TEACHES);

            teacherNodes.get("PADAM").createRelationshipTo(subjectNodes.get("INFMAN"), Relationship.TEACHES);
            teacherNodes.get("UBERT").createRelationshipTo(subjectNodes.get("INFMAN"), Relationship.TEACHES);


            teacherNodes.get("TOOJJ").createRelationshipTo(subjectNodes.get("INFDTA"), Relationship.TEACHES);
            teacherNodes.get("INGKL").createRelationshipTo(subjectNodes.get("INFDTA"), Relationship.TEACHES);

            //one teacher is the module owner
            teacherNodes.get("KLEIW").createRelationshipTo(subjectNodes.get("INFSKL"), Relationship.IS_MODULE_OWNER_OF);
            teacherNodes.get("INGKL").createRelationshipTo(subjectNodes.get("INFDEV"), Relationship.IS_MODULE_OWNER_OF);
            teacherNodes.get("PADAM").createRelationshipTo(subjectNodes.get("INFANL"), Relationship.IS_MODULE_OWNER_OF);
            teacherNodes.get("TOOJJ").createRelationshipTo(subjectNodes.get("INFSEN"), Relationship.IS_MODULE_OWNER_OF);
            teacherNodes.get("KLEIW").createRelationshipTo(subjectNodes.get("INFSLC"), Relationship.IS_MODULE_OWNER_OF);
            teacherNodes.get("UBERT").createRelationshipTo(subjectNodes.get("INFMAN"), Relationship.IS_MODULE_OWNER_OF);
            teacherNodes.get("INGKL").createRelationshipTo(subjectNodes.get("INFDTA"), Relationship.IS_MODULE_OWNER_OF);

            //To keep things easy, we start with 5 students per class
            ArrayList<Node> students = new ArrayList<>();
            for (int i = 0; i < studentNames.length; i++){
                //Creating Student ID
                String counter = Integer.toString(i);
                String studentId;
                switch (counter.length()){
                    case 1:
                        studentId = "080000";
                        break;
                    case 2:
                        studentId = "08000";
                        break;
                    case 3:
                        studentId = "0800";
                        break;
                    case 4:
                        studentId = "080";
                        break;
                    case 5:
                        studentId = "08";
                        break;
                    default:
                        studentId="";
                        break;
                }
                studentId = studentId + counter;

                Node student = graphDb.createNode(Labels.Student);
                student.setProperty(Student.STUDENT_ID ,studentId);
                student.setProperty(Student.NAME, studentNames[i]);
                student.setProperty(Student.MIDDLE_NAME, studentMiddleNames[i]);
                student.setProperty(Student.SURNAME, studentSurNames[i]);

                //Putting the students in classes
                if (i>0 && i<6){
                    student.createRelationshipTo(classA, Relationship.IS_IN);
                }else if(i>5 && i<11){
                    student.createRelationshipTo(classB, Relationship.IS_IN);
                }else if(i>10 && i<16){
                    student.createRelationshipTo(classC, Relationship.IS_IN);
                }else if(i>15 && i<21){
                    student.createRelationshipTo(classD, Relationship.IS_IN);
                }else if(i>20){
                    student.createRelationshipTo(classE, Relationship.IS_IN);
                }

                students.add(student);
            }

            for(Node n : students){
              //Create some friendships
              int numberOfFriends = rand.nextInt(5) + 1;
                for(int i = 0; i<numberOfFriends; i++){
                    int randomFriend = rand.nextInt(studentNames.length);
                    if(students.get(randomFriend)==n){
                        randomFriend++;
                    }
                    n.createRelationshipTo(students.get(randomFriend), Relationship.IS_FRIENDS_WITH);
                }

                //While we are looping through the students, we might as well create the relationship for mandatory subjects
                n.createRelationshipTo(subjectNodes.get("INFDEV"), Relationship.FOLLOWS);
                n.createRelationshipTo(subjectNodes.get("INFANL"), Relationship.FOLLOWS);
                n.createRelationshipTo(subjectNodes.get("INFSKL"), Relationship.FOLLOWS);
                n.createRelationshipTo(subjectNodes.get("INFSLC"), Relationship.FOLLOWS);

                //The student must also follow 2 other subjects
                for(int i = 0; i<2;i++){
                    switch (rand.nextInt(3) + 1){
                        case 1:
                            n.createRelationshipTo(subjectNodes.get("INFDTA"), Relationship.FOLLOWS);
                            n.createRelationshipTo(subjectNodes.get("INFMAN"), Relationship.FOLLOWS);
                            break;
                        case 2:
                            n.createRelationshipTo(subjectNodes.get("INFSEN"), Relationship.FOLLOWS);
                            n.createRelationshipTo(subjectNodes.get("INFMAN"), Relationship.FOLLOWS);
                            break;
                        case 3:
                            n.createRelationshipTo(subjectNodes.get("INFSEN"), Relationship.FOLLOWS);
                            n.createRelationshipTo(subjectNodes.get("INFDTA"), Relationship.FOLLOWS);
                            break;
                    }
                }


            }

            tx.success();
        }
    }
}
