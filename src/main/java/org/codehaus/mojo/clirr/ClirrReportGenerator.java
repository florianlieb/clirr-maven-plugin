package org.codehaus.mojo.clirr;

/*
 * Copyright 2006 The Codehaus
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

import net.sf.clirr.core.ApiDifference;
import net.sf.clirr.core.MessageTranslator;
import net.sf.clirr.core.Severity;
import org.codehaus.doxia.sink.Sink;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Generate the Clirr report.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @todo filter by severity
 */
public class ClirrReportGenerator
{
    private final ResourceBundle bundle;

    private final Sink sink;

    private boolean enableSeveritySummary;

    private final Locale locale;

    private Severity minSeverity;

    public ClirrReportGenerator( Sink sink, ResourceBundle bundle, Locale locale )
    {
        this.bundle = bundle;

        this.sink = sink;

        this.enableSeveritySummary = true;

        this.locale = locale;
    }

    public void generateReport( ClirrDiffListener listener )
    {
        doHeading();

        if ( enableSeveritySummary )
        {
            doSeveritySummary( listener );
        }

        doDetails( listener );

        sink.body_();
        sink.flush();
    }

    private void doHeading()
    {
        sink.head();
        sink.title();

        String title = bundle.getString( "report.clirr.title" );
        sink.text( title );
        sink.title_();
        sink.head_();

        sink.body();

        sink.section1();
        sink.sectionTitle1();
        sink.text( title );
        sink.sectionTitle1_();

        sink.paragraph();
        sink.text( bundle.getString( "report.clirr.clirrlink" ) + " " );
        sink.link( "http://clirr.sourceforge.net/" );
        sink.text( "Clirr" );
        sink.link_();
        sink.text( "." );

        sink.paragraph_();
    }

    private void iconInfo()
    {
        sink.figure();
        sink.figureCaption();
        sink.text( bundle.getString( "report.clirr.level.info" ) );
        sink.figureCaption_();
        sink.figureGraphics( "images/icon_info_sml.gif" );
        sink.figure_();
    }

    private void iconWarning()
    {
        sink.figure();
        sink.figureCaption();
        sink.text( bundle.getString( "report.clirr.level.warning" ) );
        sink.figureCaption_();
        sink.figureGraphics( "images/icon_warning_sml.gif" );
        sink.figure_();
    }

    private void iconError()
    {
        sink.figure();
        sink.figureCaption();
        sink.text( bundle.getString( "report.clirr.level.error" ) );
        sink.figureCaption_();
        sink.figureGraphics( "images/icon_error_sml.gif" );
        sink.figure_();
    }

    private void doSeveritySummary( ClirrDiffListener listener )
    {
        sink.section1();
        sink.sectionTitle1();
        sink.text( bundle.getString( "report.clirr.summary" ) );
        sink.sectionTitle1_();

        sink.table();

        sink.tableRow();

        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.clirr.column.severity" ) );
        sink.tableHeaderCell_();

        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.clirr.column.number" ) );
        sink.tableHeaderCell_();

        sink.tableRow_();

        sink.tableRow();
        sink.tableCell();
        iconError();
        sink.nonBreakingSpace();
        sink.text( bundle.getString( "report.clirr.level.error" ) );
        sink.tableCell_();
        sink.tableCell();
        sink.text( String.valueOf( listener.getSeverityCount( Severity.ERROR ) ) );
        sink.tableCell_();
        sink.tableRow_();

        if ( minSeverity.compareTo( Severity.WARNING ) <= 0 )
        {
            sink.tableRow();
            sink.tableCell();
            iconWarning();
            sink.nonBreakingSpace();
            sink.text( bundle.getString( "report.clirr.level.warning" ) );
            sink.tableCell_();
            sink.tableCell();
            sink.text( String.valueOf( listener.getSeverityCount( Severity.WARNING ) ) );
            sink.tableCell_();
            sink.tableRow_();
        }

        if ( minSeverity.compareTo( Severity.INFO ) <= 0 )
        {
            sink.tableRow();
            sink.tableCell();
            iconInfo();
            sink.nonBreakingSpace();
            sink.text( bundle.getString( "report.clirr.level.info" ) );
            sink.tableCell_();
            sink.tableCell();
            sink.text( String.valueOf( listener.getSeverityCount( Severity.INFO ) ) );
            sink.tableCell_();
            sink.tableRow_();
        }

        sink.table_();

        if ( minSeverity.compareTo( Severity.INFO ) > 0 )
        {
            sink.paragraph();
            sink.italic();
            sink.text( bundle.getString( "report.clirr.filtered" ) );
            sink.italic_();
            sink.paragraph_();
        }

        sink.section1_();
    }

    private void doDetails( ClirrDiffListener listener )
    {
        sink.section1();
        sink.sectionTitle1();
        sink.text( bundle.getString( "report.clirr.details" ) );
        sink.sectionTitle1_();

        List differences = listener.getApiDifferences();

        if ( !differences.isEmpty() )
        {
            doTable( differences );
        }
        else
        {
            sink.paragraph();
            sink.text( bundle.getString( "report.clirr.noresults" ) );
            sink.paragraph_();
        }

        sink.section1_();
    }

    private void doTable( List differences )
    {
        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.clirr.column.severity" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.clirr.column.message" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.clirr.column.class" ) );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( bundle.getString( "report.clirr.column.methodorfield" ) );
        sink.tableHeaderCell_();
        sink.tableRow_();

        MessageTranslator translator = new MessageTranslator();
        translator.setLocale( locale );

        for ( Iterator events = differences.iterator(); events.hasNext(); )
        {
            ApiDifference difference = (ApiDifference) events.next();

            // TODO: differentiate source and binary?
            Severity maximumSeverity = difference.getMaximumSeverity();

            if ( minSeverity.compareTo( maximumSeverity ) <= 0 )
            {
                sink.tableRow();

                sink.tableCell();
                levelIcon( maximumSeverity );
                sink.tableCell_();

                sink.tableCell();
                sink.text( difference.getReport( translator ) );
                sink.tableCell_();

                // TODO: link to file/class/method via JXR?

                sink.tableCell();
                sink.text( difference.getAffectedClass() );
                sink.tableCell_();

                sink.tableCell();
                sink.text( difference.getAffectedMethod() != null ? difference.getAffectedMethod()
                    : difference.getAffectedField() );
                sink.tableCell_();

                sink.tableRow_();
            }
        }

        sink.table_();
    }

    private void levelIcon( Severity level )
    {
        if ( Severity.INFO.equals( level ) )
        {
            iconInfo();
        }
        else if ( Severity.WARNING.equals( level ) )
        {
            iconWarning();
        }
        else if ( Severity.ERROR.equals( level ) )
        {
            iconError();
        }
    }

    public void setEnableSeveritySummary( boolean enableSeveritySummary )
    {
        this.enableSeveritySummary = enableSeveritySummary;
    }

    public void setMinSeverity( String minSeverity )
    {
        if ( "info".equals( minSeverity ) )
        {
            this.minSeverity = Severity.INFO;
        }
        else if ( "warning".equals( minSeverity ) )
        {
            this.minSeverity = Severity.WARNING;
        }
        else if ( "error".equals( minSeverity ) )
        {
            this.minSeverity = Severity.ERROR;
        }
        else
        {
            // TODO: throw an error or log
            this.minSeverity = Severity.INFO;
        }
    }
}
