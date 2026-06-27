package com.ruoyi.common.utils.html;

import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import com.ruoyi.common.utils.StringUtils;

/**
 * Sanitizes stored rich text before it is persisted or returned to clients.
 */
public final class RichTextSanitizer
{
    private static final Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

    private static final Safelist SAFELIST = Safelist.relaxed()
            .addAttributes("a", "target", "rel")
            .addAttributes("img", "alt", "title", "width", "height")
            .addAttributes("table", "border", "cellpadding", "cellspacing")
            .addAttributes("td", "colspan", "rowspan")
            .addAttributes("th", "colspan", "rowspan")
            .removeAttributes(":all", "style")
            .addProtocols("a", "href", "http", "https", "mailto", "tel")
            .addProtocols("img", "src", "http", "https");

    private RichTextSanitizer()
    {
    }

    public static String sanitize(String html)
    {
        if (StringUtils.isEmpty(html))
        {
            return html;
        }

        String cleaned = Jsoup.clean(html, "", SAFELIST, OUTPUT_SETTINGS);
        Document document = Jsoup.parseBodyFragment(cleaned);
        document.outputSettings(OUTPUT_SETTINGS);
        hardenLinks(document);
        return document.body().html();
    }

    private static void hardenLinks(Document document)
    {
        for (Element link : document.select("a[target]"))
        {
            String target = link.attr("target").toLowerCase(Locale.ROOT);
            if (!"_blank".equals(target) && !"_self".equals(target))
            {
                link.removeAttr("target");
                link.removeAttr("rel");
                continue;
            }

            if ("_blank".equals(target))
            {
                link.attr("rel", "noopener noreferrer");
            }
        }
    }
}
