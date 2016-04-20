/*
 * Copyright 2016 ASAP.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package gr.ntua.cslab.asap.client;

import java.net.URLEncoder;

import gr.ntua.cslab.asap.operators.AbstractOperator;
import gr.ntua.cslab.asap.operators.Operator;

public class OperatorClient extends RestClient{

    public OperatorClient() {
        super();
    }
	    
    public void addOperator(Operator op) throws Exception{
    	issueRequest("GET", "operators/add?opname="+op.opName+"&opString="+URLEncoder.encode(op.toKeyValues("\n"),"UTF-8"), null);
    	
    }

	public void removeOperator(String opName) throws Exception{
		issueRequest("GET", "operators/delete?opname="+opName, null);
	}
	
	public void addAbstractOperator(AbstractOperator op) throws Exception{
    	issueRequest("GET", "abstractOperators/add?opname="+op.opName+"&opString="+URLEncoder.encode(op.toKeyValues("\n"),"UTF-8"), null);
    	
    }

	public void removeAbstractOperator(String opName) throws Exception{
		issueRequest("GET", "abstractOperators/delete?opname="+opName, null);
	}

	public String[] checkMatches(AbstractOperator aop) throws Exception{
		// TODO Auto-generated method stub
		//List<String> ret;
		String out= issueRequest("GET", "abstractOperators/checkMatches?opname="+aop.opName+"&opString="+URLEncoder.encode(aop.toKeyValues("\n"),"UTF-8"), null);
		String[] ret = out.split("&&");
		//System.out.println(out);
		for (int i = 0; i < ret.length; i++) {
			//System.out.println(ret[i]);
		}
		return ret;
	}
}
