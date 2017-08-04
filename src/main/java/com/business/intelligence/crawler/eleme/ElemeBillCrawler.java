package com.business.intelligence.crawler.eleme;

import com.business.intelligence.dao.ElemeDao;
import com.business.intelligence.model.Authenticate;
import com.business.intelligence.model.ElemeModel.ElemeBill;
import com.business.intelligence.util.DateUtils;
import com.business.intelligence.util.HttpClientUtil;
import com.business.intelligence.util.WebUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

/**
 * Created by Tcqq on 2017/7/25.
 * 账单记录 GET请求
 */
@Component
public class ElemeBillCrawler extends ElemeCrawler{
    //默认抓取前一天的，具体值已经在父类设置
    private Date crawlerDate = super.crawlerDate;
    //用户信息
    private Authenticate authenticate;
    @Autowired
    private ElemeDao elemeDao;
    private HttpClient httpClient = super.httpClient;

    private static final String URL ="https://httpizza.ele.me/hydros/bill/list";

    @Override
    public void doRun() {
        List<LinkedHashMap<String, Object>> billText = getBillText(login());
        List<ElemeBill> billList = getElemeBillBeans(billText);
        for(ElemeBill elemeBill : billList){
            elemeDao.insertBill(elemeBill);
        }
    }

    /**
     * 通过爬虫获得所有的对应日期的账单分析
     * @param client
     * @return
     */
    public List<LinkedHashMap<String, Object>> getBillText(CloseableHttpClient client){
        CloseableHttpResponse execute = null;
        Map<String,String> params = new HashMap<>();
        params.put("beginDate",String.valueOf(crawlerDate.getTime()));
        params.put("endDate",String.valueOf(crawlerDate.getTime()));
        params.put("limit","10");
        params.put("loginRestaurantId","150148671");
        params.put("offset","0");
        params.put("restaurantId","150148671");
        params.put("status","3");
        params.put("token","30a23e32be094eebfa1e93ddc59eed83");
        String url2 = URL+HttpClientUtil.buildParamString(params);
        HttpGet get = new HttpGet(url2);
        try {
            execute = client.execute(get);
            HttpEntity entity = execute.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            List<LinkedHashMap<String, Object>> mapsByJsonPath = WebUtils.getMapsByJsonPath(result, "$.bills");
            return mapsByJsonPath;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (execute != null){
                    execute.close();
                }
                if(client != null){
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取ElemeBill实体类
     * @param billList
     * @return
     */
    public List<ElemeBill> getElemeBillBeans(List<LinkedHashMap<String, Object>> billList){
        List<ElemeBill> list = new ArrayList<>();
        for(LinkedHashMap<String,Object> map : billList){
            ElemeBill elemeBill = new ElemeBill();
            elemeBill.setClosingDate(String.valueOf(DateUtils.long2Date((Long)map.get("closingDate"))));
            elemeBill.setIncome((String)map.getOrDefault("income","无"));
            elemeBill.setExpense((String)map.getOrDefault("expense","无"));
            elemeBill.setDeductAmount((String)map.getOrDefault("deductAmount","无"));
            elemeBill.setDueAmount((String)map.getOrDefault("dueAmount","无"));
            elemeBill.setPayAmount((String)map.getOrDefault("payAmount","无"));
            elemeBill.setPaymentDate(DateUtils.long2Date((Long)map.getOrDefault("paymentDate",null)));
            elemeBill.setShopId(150148671l);
            list.add(elemeBill);
        }
        return list;
    }



}