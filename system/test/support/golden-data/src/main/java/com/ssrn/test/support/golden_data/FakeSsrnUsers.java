package com.ssrn.test.support.golden_data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FakeSsrnUsers extends SsrnUsers {
    private static final List<SsrnUser> fakeUsers = new ArrayList<>();

    public static final SsrnUser JOHN_DOE = createFakeUser("1000000000", "john.doe@email.com", "John Doe", "john.doe@email.com", false);
    public static final SsrnUser BILLY_BOB = createFakeUser("1000000001", "billy.bob@email.com", "Billy Bob", "billy.bob@email.com", false);
    public static final SsrnUser LUCY_JONES = createFakeUser("1000000002", "lucy.jones@email.com", "Lucy Jones", "lucy.jones@email.com", false);
    public static final SsrnUser TIM_DUNCAN = createFakeUser("1000000003", "tim.duncan@ssrn.com", "Tim Duncan", "tim.duncan@ssrn.com", false);
    public static final SsrnUser JAMES_BROWN = createFakeUser("1000000004", "james.brown@ssrn.com", "James Brown", "james.brown@ssrn.com", false);
    public static final SsrnUser JOE_BLACK = createFakeUser("1000000005", "joe.black@ssrn.com", "Joe Black", "joe.black@ssrn.com", false);
    public static final SsrnUser CHANGE_ME = createFakeUser("1000000006", "change.me@ssrn.com", "Change Me", "change.me@ssrn.com", false);
    public static final SsrnUser AN_ADMIN = createFakeUser("1000000007", "an.admin@email.com", "An Admin", "an.admin@email.com", false);
    public static final SsrnUser REMOVED_AUTHOR = createFakeUser("1000000008", "removed@email.com", "Removed Author", "removed@email.com", true);


    public static Stream<SsrnUser> all() {
        return fakeUsers.stream();
    }

    private static SsrnUser createFakeUser(String id, String username, String publicDisplayName, String emailAddress, boolean removed) {
        return createUserIn(fakeUsers, id, publicDisplayName, username, emailAddress, removed);
    }
}
