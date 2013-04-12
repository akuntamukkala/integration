package com.vpn.integration.route.rfq.beanprocessor;
public class MyOrderService {

    private static int counter;

    /**
     * We just handle the order by returning a id line for the order
     */
    public String handleOrder(String line) {
       
        return "(id=" + ++counter + ",item=" + line + ")";
    }

    /**
     * We use the same bean for building the combined response to send
     * back to the original caller
     */
    public String buildCombinedResponse(String line) {
       
    	
    	System.out.println("Response[" + line + "]");
    			
        return "Response[" + line + "]";
    }
}