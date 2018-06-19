package com.ssrn.test.support.golden_data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.ssrn.test.support.golden_data.FakeSsrnUsers.BILLY_BOB;

public class SsrnPapers {
    private static final List<SsrnPaper> papers = new ArrayList<>();

    static {
        createPaper("64", "Protection for Whom? Creditor Conflicts in Bankruptcy", null, false, false, false, "APPROVED");
        createPaper("62", "The Performance of Global Bond Mutual Funds", null, false, false, false, "APPROVED", RealSsrnUsers.USER_61);
        createPaper("1156617", "1156617", null, false, false, false, "IN DRAFT", RealSsrnUsers.USER_1);
        createPaper("895", "1156617", null, false, false, false, "REJECTED", RealSsrnUsers.USER_9);
        createPaper("296681", "1156617", null, false, false, false, "REJECTED", RealSsrnUsers.USER_3);
        createPaper("445862", "1156617", null, false, false, false, "DELETED", RealSsrnUsers.USER_6, RealSsrnUsers.USER_9);
        createPaper("230405", "Financial Institutions", null, false, false, false, "DELETED", RealSsrnUsers.USER_2);
    }

    public static final SsrnPaper PAPER_42 = createPaper("42", "Market-Adjusted Options for Executive Compensation", null, false, false, false, "DELETED", RealSsrnUsers.USER_35974, RealSsrnUsers.USER_35976 );
    public static final SsrnPaper PAPER_87 = createPaper("87", "Messages from Market to Management: The Case of Ipos", null, false, false, false, "APPROVED", RealSsrnUsers.USER_90);
    public static final SsrnPaper PAPER_45 = createPaper("45", "Effects of the Real Plan on the Brazilian Banking System", null, false, false, false, "APPROVED", RealSsrnUsers.USER_47, RealSsrnUsers.USER_771);
    public static final SsrnPaper PAPER_48 = createPaper("48", "Dynamic Risk Shifting, Debt Maturity and Negotiation Tactics", null, false, false, false, "APPROVED", RealSsrnUsers.USER_51, RealSsrnUsers.USER_50);
    public static final SsrnPaper SECOND_PAPER_IN_SSRN = PAPER_45;
    public static final SsrnPaper PAPER_52 = createPaper("52", "The Cost of Equity and Exchange Listing: Evidence from the French Stock Market", null, false, false, false, "APPROVED", RealSsrnUsers.USER_54, RealSsrnUsers.USER_1341);
    public static final SsrnPaper PAPER_535 = createPaper("535", "Law and Borders - the Rise of Law in Cyberspace", "Internet, Cyberspace, regulation", false, false, false, "APPROVED", RealSsrnUsers.USER_536, RealSsrnUsers.USER_537);

    public static final SsrnPaper PAPER_20005 = createPaper("20005", "a paper about the Market", null, false, false, false, "SUBMITTED", BILLY_BOB, FakeSsrnUsers.LUCY_JONES, FakeSsrnUsers.JOHN_DOE);

    private static SsrnPaper createPaper(String id, String title, String keywords, boolean isPrivate, boolean isIrrelevant, boolean isRestricted, String submissionStage, SsrnUser... authors) {
        SsrnPaper ssrnPaper = new SsrnPaper(id, title, keywords, isPrivate, isIrrelevant, isRestricted, submissionStage, authors);
        papers.add(ssrnPaper);
        return ssrnPaper;
    }

    public static Stream<SsrnPaper> all() {
        return papers.stream();
    }
}
