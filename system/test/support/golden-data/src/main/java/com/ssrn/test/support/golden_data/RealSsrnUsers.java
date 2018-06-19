package com.ssrn.test.support.golden_data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class RealSsrnUsers extends SsrnUsers {
    private static List<SsrnUser> realUsers = new ArrayList<>();

    public static final SsrnUser USER_1 = createRealUser("1", "Public User");
    public static final SsrnUser USER_2 = createRealUser("2", "SSRN System");
    public static final SsrnUser USER_3 = createRealUser("3", "Gregory J. Gordon");
    public static final SsrnUser USER_6 = createRealUser("6", "Gregg Gordon");
    public static final SsrnUser USER_9 = createRealUser("9", "Michael C. Jensen");
    public static final SsrnUser USER_47 = createRealUser("47", "Rubens Penha Penha Penha Cysne");
    public static final SsrnUser USER_54 = createRealUser("54", "Michel Dubois");
    public static final SsrnUser USER_90 = createRealUser("90", "Jos van Bommel");
    public static final SsrnUser USER_771 = createRealUser("771", "Sergio Gustavo Silveira da Costa");
    public static final SsrnUser USER_1341 = createRealUser("1341", "C. Ertur");
    public static final SsrnUser USER_35974 = createRealUser("35974", "James Angel");
    public static final SsrnUser USER_35976 = createRealUser("35976", "Douglas McCabe");
    public static final SsrnUser USER_50 = createRealUser("50", "Charles J. J. J. Cuny");
    public static final SsrnUser USER_51 = createRealUser("51", "Eli Talmor");
    static final SsrnUser USER_61 = createRealUser("61", "Miranda Lam");
    public static final SsrnUser USER_536 = createRealUser("536", "David R. Johnson");;
    public static final SsrnUser USER_537 = createRealUser("537", "David G. Post");;

    private static SsrnUser createRealUser(String id, String name) {
        return createUserIn(realUsers, id, name, "default@email.com", "default@email.com", false);
    }

    public static Stream<SsrnUser> all() {
        return realUsers.stream();
    }
}
