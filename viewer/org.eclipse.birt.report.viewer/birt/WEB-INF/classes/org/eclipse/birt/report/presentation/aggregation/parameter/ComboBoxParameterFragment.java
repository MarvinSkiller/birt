/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.presentation.aggregation.parameter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.report.IBirtConstants;
import org.eclipse.birt.report.context.ScalarParameterBean;
import org.eclipse.birt.report.context.ViewerAttributeBean;
import org.eclipse.birt.report.model.api.ScalarParameterHandle;
import org.eclipse.birt.report.model.api.util.ParameterValidationUtil;
import org.eclipse.birt.report.service.api.IViewerReportDesignHandle;
import org.eclipse.birt.report.service.api.IViewerReportService;
import org.eclipse.birt.report.service.api.InputOptions;
import org.eclipse.birt.report.service.api.ParameterDefinition;
import org.eclipse.birt.report.service.api.ParameterGroupDefinition;
import org.eclipse.birt.report.service.api.ParameterSelectionChoice;
import org.eclipse.birt.report.service.api.ReportServiceException;
import org.eclipse.birt.report.utility.ParameterAccessor;

/**
 * Fragment help rendering scalar parameter.
 * <p>
 * 
 * @see org.eclipse.birt.report.presentation.aggregation.BaseFragment
 */
public class ComboBoxParameterFragment extends ScalarParameterFragment
{

	/**
	 * Protected constructor.
	 * 
	 * @param parameter
	 *            parameter definition reference.
	 */
	public ComboBoxParameterFragment( ParameterDefinition parameter )
	{
		super( parameter );
	}

	protected void prepareParameterBean( HttpServletRequest request,
			IViewerReportService service, ScalarParameterBean parameterBean,
			Locale locale ) throws ReportServiceException
	{
		ViewerAttributeBean attrBean = (ViewerAttributeBean) request
				.getAttribute( IBirtConstants.ATTRIBUTE_BEAN );
		assert attrBean != null;

		InputOptions options = new InputOptions( );
		options.setOption( InputOptions.OPT_REQUEST, request );

		Collection selectionList = null;

		ParameterDefinition paramDef = parameterBean.getParameter( );
		if ( paramDef.getGroup( ) != null && paramDef.getGroup( ).cascade( ) )
		{
			Map paramValues = attrBean.getParameters( );
			selectionList = getParameterSelectionListForCascadingGroup(
					attrBean.getReportDesignHandle( request ), service,
					paramValues, options );
		}
		else
			selectionList = service.getParameterSelectionList( attrBean
					.getReportDesignHandle( request ), options, parameter
					.getName( ) );

		parameterBean.setValueInList( false );

		if ( selectionList != null )
		{
			// Get Scalar parameter handle
			ScalarParameterHandle parameterHandle = (ScalarParameterHandle) attrBean
					.findParameter( parameter.getName( ) );

			for ( Iterator iter = selectionList.iterator( ); iter.hasNext( ); )
			{
				ParameterSelectionChoice selectionItem = (ParameterSelectionChoice) iter
						.next( );

				// Convert parameter value using locale format
				String value = ParameterValidationUtil.getDisplayValue(
						parameterHandle.getDataType( ), parameterHandle
								.getPattern( ), selectionItem.getValue( ),
						attrBean.getLocale( ) );

				String label = selectionItem.getLabel( );
				label = ( label == null || label.length( ) <= 0 )
						? value
						: label;
				label = ParameterAccessor.htmlEncode( label );

				if ( label != null )
				{
					parameterBean.getSelectionList( ).add( label );
					parameterBean.getSelectionTable( ).put( label, value );
				}

				if ( value != null && value.equals( parameterBean.getValue( ) ) )
				{
					parameterBean.setValueInList( true );
				}
			}
		}
	}

	private Collection getParameterSelectionListForCascadingGroup(
			IViewerReportDesignHandle design, IViewerReportService service,
			Map paramValues, InputOptions options )
			throws ReportServiceException
	{

		ParameterGroupDefinition group = (ParameterGroupDefinition) parameter
				.getGroup( );
		int index = group.getParameters( ).indexOf( parameter );
		Object[] groupKeys = new Object[index];
		for ( int i = 0; i < index; i++ )
		{
			ParameterDefinition def = (ParameterDefinition) group
					.getParameters( ).get( i );
			String parameterName = def.getName( );
			groupKeys[i] = paramValues.get( parameterName );
			if ( groupKeys[i] == null )
			{
				groupKeys[i] = service.getParameterDefaultValue( design,
						parameterName, options );
			}
		}
		return service.getSelectionListForCascadingGroup( design, group
				.getName( ), groupKeys, options );
	}
}