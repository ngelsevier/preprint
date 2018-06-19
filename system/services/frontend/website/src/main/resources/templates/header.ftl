<#import "/templates/components/buttons.ftl" as buttons />

<#assign authPath="/rest/user/whoami">
<header data-authenticated="false" data-auth-base="${authBaseUrl}" data-auth-path="${authPath}">
    <div class="wrapper">
        <nav class="main-menu">
            <ul class="reset" data-open="false">
                <li>
                    <a aria-label="Home" class="logo" href="https://ssrn.com/en/">
                        <img alt="SSRN Logo" src="images/ssrn-logo.png"/>
                    </a>
                </li>
                <li>
                    <a href="https://papers.ssrn.com/sol3/DisplayJournalBrowse.cfm">Browse</a>
                </li>
                <li>
                    <a href="https://www.ssrn.com/en/index.cfm/subscribe/">Subscriptions</a>
                </li>
                <li class="focus-children">
                    <a>Rankings</a>
                    <ul>
                        <li>
                            <a href="https://hq.ssrn.com/rankings/Ranking_display.cfm?TRN_gID=10">Top Papers</a>
                        </li>
                        <li>
                            <a href="https://www.ssrn.com/en/index.cfm/top-authors/">Top Authors</a>
                        </li>
                        <li>
                            <a href="https://www.ssrn.com/en/index.cfm/top-organizations/">Top Organizations</a>
                        </li>
                    </ul>
                </li>
                <li>
                    <a href="https://hq.ssrn.com/submissions/CreateNewAbstract.cfm">Submit a paper</a>
                </li>
                <li>
                    <a href="https://hq.ssrn.com/Library/myLibrary.cfm" title="formerly My Briefcase" target="_blank">My
                        Library</a>
                </li>
                <li>
                    <a href="http://ssrnblog.com/">Blog</a>
                </li>
            </ul>
        </nav>
        <nav class="user-menu">
            <ul class="reset">
                <li>
                    <a aria-label="Shopping Cart" class="cart" href="https://papers.ssrn.com/sol3/ShoppingCart.cfm">
                        <i class="icon icon-gizmo-shopping-cart"></i>
                    </a>
                </li>
                <li>
                    <a class="profile" role="button">
                        <span>${userName!"Public User"}</span>
                        <i class="icon icon-gizmo-person" aria-hidden="true"></i>
                    </a>
                </li>
                <li><@buttons.link href="https://hq.ssrn.com/login/pubsigninjoin.cfm" text="Register"/></li>
                <li><@buttons.link href="https://hq.ssrn.com/login/pubsigninjoin.cfm" text="Sign In"/></li>
                <li>
                    <a class="hamburger">
                        <i class="icon icon-nav-menu"></i>
                    </a>
                </li>
            </ul>
        </nav>
        <nav class="auth-menu" data-open="false">
            <ul class="reset">
                <li>
                    <a href="https://hq.ssrn.com/UserHome.cfm">User Home</a>
                </li>
                <li>
                    <a class="append-uid" href="https://hq.ssrn.com/Participant.cfm?rectype=edit&perinf=y&partid=">Personal Info</a>
                </li>
                <li>
                    <a class="append-uid" href="https://hq.ssrn.com/Affiliations/AffiliationList.cfm?partid=">Affiliations</a>
                </li>
                <li>
                    <a class="append-uid" href="https://hq.ssrn.com/Subscriptions.cfm?partId=">Subscriptions</a>
                </li>
                <li>
                    <a class="append-uid" href="https://hq.ssrn.com/submissions/MyPapers.cfm?partid=">My Papers</a>
                </li>
                <li>
                    <a href="https://hq.ssrn.com/Library/myLibrary.cfm">My Library</a>
                </li>
                <li>
                    <a href="https://hq.ssrn.com/SignOut.cfm?proc=notme">
                        <i class="icon icon-gizmo-log-out" aria-hidden="true"></i>
                        Sign out
                    </a>
                </li>
            </ul>
        </nav>
    </div>
</header>