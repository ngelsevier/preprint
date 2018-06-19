<#-- @ftlvariable name="" type="com.ssrn.frontend.website.search.SearchPageView"  -->
<#import "/templates/layout.ftl" as layout />

<#assign styles>
<link type="text/css" rel="stylesheet" href="css/search.css?v=6"/>
</#assign>

<#assign scripts>
    <script type="text/javascript" src="js/support-mailto.js?v=2" async="async"></script>
</#assign>

<@layout.layout title="Fast Search" displaySearchBox=false styleExtensions=styles scriptExtensions=scripts>
<section class="search">
    <div class="wrapper central">
        <h1>Fast Search</h1>

        <form>
            <div class="search-box-wrapper centered">
                <input aria-label="Search" type="text" placeholder="Search SSRN" name="query"/>
                <button aria-label="Search" type="submit">
                    <i class="icon icon-gizmo-search"></i>
                </button>
            </div>
        </form>

        <div class="readme centered">
            <div class="panel left-align">
                <h4>What is the Fast Search?</h4>
                <p>SSRN's new Fast Search functionality provides a fast Title & Keyword search across Papers, along with
                    an Author Search, returning results ordered by relevance.
                </p>
            </div>
        </div>
    </div>
</section>
</@layout.layout>