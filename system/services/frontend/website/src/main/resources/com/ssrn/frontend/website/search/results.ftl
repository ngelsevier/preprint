<#ftl auto_esc=false>
<#-- @ftlvariable name="" type="com.ssrn.frontend.website.search.SearchResultsPageView"  -->
<#import "/templates/layout.ftl" as layout />
<#import "/templates/components/buttons.ftl" as buttons />

<#assign styles>
<link type="text/css" rel="stylesheet" href="css/search.css?v=6"/>
</#assign>

<#assign scripts>
    <script type="text/javascript" src="js/lazy.js" async="async"></script>
</#assign>

<@layout.layout title="Fast Search" displaySearchBox=true styleExtensions=styles scriptExtensions=scripts>
<section class="results">
    <div class="wrapper central">
        <h1 class="central">Search results</h1>
        <div class="panel left-align">
            <p class="large-text search-terms">You searched: ${searchQuery}</p>
            <p class="large-text search-result-count"><span>${numberOfSearchResults}</span> results returned</p>
            <#list searchResults>
                <ol class="reset line-up">
                    <#items as searchResult>

                        <li class="${searchResult.type}">
                            <#if searchResult.type == "author">
                                <div>
                                    <div class="circle-image-wrapper">
                                        <img alt="Author" class="lazy" data-lazy-src="${searchResult.authorImageUrl}"/>
                                    </div>
                                    <a class="primary" href="${searchResult.profilePageUrl}">
                                        <span>${searchResult.authorName}</span>
                                    </a>
                                </div>
                            <#else>
                                <div>
                                    <i class="icon icon-gizmo-file"></i>
                                    <div>
                                        <a class="primary" href="${searchResult.articlePageUrl}">
                                            <span>${searchResult.title}</span>
                                        </a>

                                        <p class="author-list">
                                        <#list searchResult.authors as author>
                                            <#if author.authorName?? >
                                                <span><a href="${author.profilePageUrl}">${author.authorName}</a></span>
                                            </#if>
                                        </#list>
                                        </p>
                                        <#if searchResult.keywords??>
                                            <p class="keywords">${searchResult.keywords}</p>
                                        </#if>
                                    </div>
                                </div>
                            </#if>
                        </li>
                    </#items>
                </ol>
            </#list>

            <#if showPrev || showNext>
                <div class="results-pagination ${(!showPrev && showNext)?then('next-only', '')}">
                    <#if showPrev==true>
                        <@buttons.iconLink href="?query=${searchQuery}&from=${prevFrom}" text="Prev" direction="left" additionalAttributes="data-direction=prev"/>
                    </#if>
                    <#if showNext==true>
                        <@buttons.iconLink href="?query=${searchQuery}&from=${nextFrom}" text="Next" additionalAttributes="data-direction=next"/>
                    </#if>
                </div>
            </#if>
        </div>
    </div>
</section>
</@layout.layout>