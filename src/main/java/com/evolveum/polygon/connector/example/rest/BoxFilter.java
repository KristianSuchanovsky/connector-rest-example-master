package com.evolveum.polygon.connector.example.rest;

import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;

public class BoxFilter {
	
	private static AttributeFilter getFilter(String filterType, String resourceName, Uid userUid){
		Filter filter = getFilter(filterType, resourceName, userUid);
		
		
		
		return (AttributeFilter) filter;
	}

}
