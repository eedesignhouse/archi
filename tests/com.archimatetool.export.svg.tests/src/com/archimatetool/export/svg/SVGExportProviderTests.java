/**
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */
package com.archimatetool.export.svg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.archimatetool.editor.diagram.IImageExportProvider.IExportDialogAdapter;
import com.archimatetool.tests.TestUtils;


@SuppressWarnings("nls")
public class SVGExportProviderTests {
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(SVGExportProviderTests.class);
    }
    
    private SVGExportProvider provider;
    private Shell shell;
    
    private IFigure rootFigure;
    
    @Before
    public void runOnceBeforeEachTest() {
        provider = new SVGExportProvider();
        shell = new Shell();
        
        // Set prefs to defaults
        IPreferenceStore store = ExportSVGPlugin.getDefault().getPreferenceStore();
        store.setValue(SVGExportProvider.PREFS_VIEWBOX_ENABLED, false);
        store.setValue(SVGExportProvider.PREFS_VIEWBOX, "");
        
        rootFigure = new FreeformLayer();
        rootFigure.setBounds(new Rectangle(0, 0, 500, 500));
    }
    
    @After
    public void runOnceAfterEachTest() {
        shell.dispose();
    }

    @Test
    public void testExport() throws Exception {
        File tmp = TestUtils.createTempFile(null);
        provider.init(mock(IExportDialogAdapter.class), shell, rootFigure);
        provider.export(SVGExportProvider.SVG_IMAGE_EXPORT_PROVIDER, tmp);
        assertTrue(tmp.exists());
        assertTrue(tmp.length() > 100);
        // How do you test the integrity of an SVG file? Look at it in a viewer? ;-)
    }

    @Test
    public void testGetViewportBounds() {
        provider.init(mock(IExportDialogAdapter.class), shell, rootFigure);

        // Default blank size
        assertEquals(new Rectangle(0, 0, 100, 100), provider.getViewportBounds());
        
        // Add a child figure
        IFigure childFigure = new Figure();
        rootFigure.add(childFigure);

        // Bounds is expanded by 10 pixesls each side
        childFigure.setBounds(new Rectangle(0, 0, 100, 50));
        assertEquals(new Rectangle(-10, -10, 120, 70), provider.getViewportBounds());
        
        // Bounds is small figure and expanded by 10 pixesls each side
        childFigure.setBounds(new Rectangle(200, 200, 128, 52));
        assertEquals(new Rectangle(190, 190, 148, 72), provider.getViewportBounds());
    }

    @Test
    public void testCreateDocument() {
        Document document = provider.createDocument();
        assertNotNull(document);
        assertNotNull(document.getDocumentElement());
    }
    
    @Test
    public void testSetViewBoxAttribute() {
        Document document = provider.createDocument();
        provider.setViewBoxAttribute(document.getDocumentElement(), 12, 13, 14, 15);
        assertEquals("12 13 14 15", document.getDocumentElement().getAttribute("viewBox"));
    }
    
    @Test
    public void testInit() {
        provider.init(mock(IExportDialogAdapter.class), shell, rootFigure);
        assertTrue(shell.getChildren().length > 0);
    }

    @Test
    public void testSavePreferences() {
        provider.init(mock(IExportDialogAdapter.class), shell, rootFigure);

        provider.fSetViewboxButton.setSelection(true);
        provider.fSpinner1.setSelection(1);
        provider.fSpinner2.setSelection(2);
        provider.fSpinner3.setSelection(3);
        provider.fSpinner4.setSelection(4);
        
        provider.savePreferences();

        IPreferenceStore store = ExportSVGPlugin.getDefault().getPreferenceStore();
        
        assertTrue(store.getBoolean(SVGExportProvider.PREFS_VIEWBOX_ENABLED));
        assertEquals("1 2 3 4", store.getString(SVGExportProvider.PREFS_VIEWBOX));
    }
    
    @Test
    public void testPreferencesWereLoaded() {
        IPreferenceStore store = ExportSVGPlugin.getDefault().getPreferenceStore();
        store.setValue(SVGExportProvider.PREFS_VIEWBOX_ENABLED, true);
        store.setValue(SVGExportProvider.PREFS_VIEWBOX, "5 6 7 8");
        
        provider.init(mock(IExportDialogAdapter.class), shell, rootFigure);
        
        assertTrue(provider.fSetViewboxButton.getSelection());
        assertEquals(5, provider.fSpinner1.getSelection());
        assertEquals(6, provider.fSpinner2.getSelection());
        assertEquals(7, provider.fSpinner3.getSelection());
        assertEquals(8, provider.fSpinner4.getSelection());
    }
}
