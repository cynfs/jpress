/**
 * Copyright (c) 2016-2019, Michael Yang 杨福海 (fuhai999@gmail.com).
 * <p>
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.web.render;

import com.jfinal.core.JFinal;
import com.jfinal.render.Render;
import com.jfinal.render.RenderManager;
import com.jfinal.template.Engine;
import io.jboot.utils.StrUtils;
import io.jboot.web.render.RenderHelpler;
import io.jpress.core.template.Template;
import io.jpress.core.template.TemplateManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Package io.jpress.web.render
 */
public class TemplateRender extends Render {

    private static Engine engine;
    private static final String contentType = "text/html; charset=" + getEncoding();

    private Engine getEngine() {
        if (engine == null) {
            engine = RenderManager.me().getEngine();
        }
        return engine;
    }

    private static String cdnDomain;

    public static void initCdnDomain(String cdnDomain) {
        TemplateRender.cdnDomain = cdnDomain;
    }


    public TemplateRender(String view) {
        this.view = view;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public void render() {
        response.setContentType(getContentType());

        Map<Object, Object> data = new HashMap<Object, Object>();
        for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            data.put(attrName, request.getAttribute(attrName));
        }

        String html = getEngine().getTemplate(view).renderToString(data);
        html = replaceSrcTemplateSrcPath(html);

        RenderHelpler.actionCacheExec(html, contentType);
        RenderHelpler.renderHtml(response, html, contentType);

    }


    public String toString() {
        return view;
    }


    public static String replaceSrcTemplateSrcPath(String content) {
        if (StrUtils.isBlank(content)) {
            return content;
        }


        Document doc = Jsoup.parse(content);

        Elements jsElements = doc.select("script[src]");
        replace(jsElements, "src");

        Elements imgElements = doc.select("img[src]");
        replace(imgElements, "src");

        Elements lazyElements = doc.select("img[data-original]");
        replace(lazyElements, "data-original");

        Elements linkElements = doc.select("link[href]");
        replace(linkElements, "href");

        return doc.toString();

    }

    private static void replace(Elements elements, String attrName) {
        Iterator<Element> iterator = elements.iterator();
        Template template = TemplateManager.me().getCurrentTemplate();
        while (iterator.hasNext()) {

            Element element = iterator.next();
            String url = element.attr(attrName);

            if (StrUtils.isBlank(url)
                    || url.startsWith("//")
                    || url.toLowerCase().startsWith("http")
                    || element.hasAttr("cdn-exclude")) {
                continue;
            }

            if (url.startsWith("/")) {
                url = JFinal.me().getContextPath() + url;
                if (cdnDomain != null) {
                    url = cdnDomain + url;
                }
                element.attr(attrName, url);
                continue;
            }


            if (url.startsWith("./")) {
                url = JFinal.me().getContextPath() + template.getWebAbsolutePath() + url.substring(1);
            } else {
                url = JFinal.me().getContextPath() + template.getWebAbsolutePath() + "/" + url;
            }

            if (cdnDomain == null) {
                element.attr(attrName, url);
            } else {
                element.attr(attrName, cdnDomain + url);
            }


        }
    }

}
