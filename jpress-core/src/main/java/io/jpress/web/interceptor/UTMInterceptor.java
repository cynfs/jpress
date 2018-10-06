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
package io.jpress.web.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import io.jboot.utils.EncryptCookieUtils;
import io.jboot.utils.RequestUtils;
import io.jboot.utils.StrUtils;
import io.jpress.JPressConsts;
import io.jpress.model.Utm;
import io.jpress.service.UtmService;

import javax.inject.Inject;

/**
 * @author Michael Yang 杨福海 （fuhai999@gmail.com）
 * @version V1.0
 * @Title: utm拦截器
 * @Package io.jpress.web
 */
public class UTMInterceptor implements Interceptor {

    @Inject
    private UtmService utmService;


    @Override
    public void intercept(Invocation inv) {

        Controller ctrl = inv.getController();

        Utm utm = new Utm();
        utm.setId(StrUtils.uuid());
        utm.setActionKey(ctrl.getRequest().getRequestURI());
        utm.setActionQuery(ctrl.getRequest().getQueryString());
        utm.setIp(RequestUtils.getIpAddress(ctrl.getRequest()));
        utm.setAgent(RequestUtils.getUserAgent(ctrl.getRequest()));
        utm.setReferer(RequestUtils.getReferer(ctrl.getRequest()));


        String uid = EncryptCookieUtils.get(ctrl, JPressConsts.COOKIE_UID);
        if (StrUtils.isNotBlank(uid)) {
            utm.setUserId(Long.valueOf(uid));
        }

        /**
         * 当用户未登录的情况下，创建匿名记录
         */
        else {
            //anonym
            String anonym = EncryptCookieUtils.get(ctrl, JPressConsts.COOKIE_ANONYM);
            if (StrUtils.isNotBlank(anonym)) {
                utm.setAnonym(anonym);
            } else {
                EncryptCookieUtils.put(ctrl, JPressConsts.COOKIE_ANONYM, StrUtils.uuid(), 60 * 60 * 24 * 365);
            }
        }

        utm.setSource(ctrl.getPara("source"));
        utm.setMedium(ctrl.getPara("medium"));
        utm.setCampaign(ctrl.getPara("campaign"));
        utm.setContent(ctrl.getPara("content"));
        utm.setTerm(ctrl.getPara("term"));

        utmService.doRecord(utm);
        inv.invoke();

    }
}
