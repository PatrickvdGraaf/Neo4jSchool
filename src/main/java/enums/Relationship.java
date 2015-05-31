package enums;

import org.neo4j.graphdb.RelationshipType;

/**
 * This class is used to...
 * Created by Patrick van de Graaf
 */
public enum Relationship implements RelationshipType{
    TEACHES,
    FOLLOWS,
    IS_IN,
    MENTORS,
    IS_FRIENDS_WITH,
    IS_IN_GROUP_WITH;
    public static final String FROM = "from";
}
