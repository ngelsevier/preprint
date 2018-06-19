package com.ssrn.test.support.golden_data;

import java.util.List;
import java.util.stream.Stream;

public abstract class SsrnUsers {

    static SsrnUser createUserIn(List<SsrnUser> users, String id, String name, String username, String emailAddress, boolean removed) {
        SsrnUser ssrnUser = new SsrnUser(id, username, name, emailAddress, removed);
        users.add(ssrnUser);
        return ssrnUser;
    }

    public static Stream<SsrnUser> all() {
        return Stream.concat(RealSsrnUsers.all(), FakeSsrnUsers.all());
    }
}
