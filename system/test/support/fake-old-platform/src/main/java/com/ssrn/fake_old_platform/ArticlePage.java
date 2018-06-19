package com.ssrn.fake_old_platform;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/")
@Produces("text/html; charset=utf-8")
public class ArticlePage {
    private final PaperRepository paperRepository;

    ArticlePage(PaperRepository paperRepository) {
        this.paperRepository = paperRepository;
    }

    @GET
    @Path("/abstract={abstractId}")
    public ArticlePageView submitAbstract(@PathParam("abstractId") String abstractId) {
        Paper paper = paperRepository.getById(Integer.parseInt(abstractId));
        return new ArticlePageView(paper == null ? "No record found" : paper.getTitle());
    }
}
