package com.github.invictum.test.unified.data.provider;

import com.github.invictum.test.unified.data.provider.pages.ChildPage;
import com.github.invictum.test.unified.data.provider.panels.TestChildPanel;
import com.github.invictum.unified.data.provider.UnifiedDataProvider;
import com.github.invictum.unified.data.provider.UnifiedDataProviderFactory;
import com.github.invictum.unified.data.provider.parsers.YamlParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UnifiedDataProviderFactory.class)
public class UnifiedDataProviderFactoryTest {

    @Mock
    private YamlParser parserMock;
    private UnifiedDataProvider dataProvider;

    @Before
    public void beforeTest() {
        dataProvider = new UnifiedDataProvider();
        dataProvider.setName("Name");
        dataProvider.setBase("base");
        when(parserMock.load(anyString())).thenReturn(dataProvider);
        Whitebox.setInternalState(UnifiedDataProviderFactory.class, "parser", parserMock);
    }

    @After
    public void afterTest() {
        Whitebox.setInternalState(UnifiedDataProviderFactory.class, "locatorProviders", new HashMap<>());
    }

    @Test
    public void getInstanceTest() {
        UnifiedDataProvider actual = UnifiedDataProviderFactory.getInstance(new Object());
        assertThat("Returned provider is wrong.", actual, equalTo(dataProvider));
        verify(parserMock, times(1)).load(anyString());
    }

    @Test
    public void getInstanceInheritPanelTest() {
        /** Prepare parent data. */
        Map<String, String> parentLocators = new HashMap<>();
        parentLocators.put("parent1", "parent1");
        parentLocators.put("parent2", "parent2");
        UnifiedDataProvider parentData = new UnifiedDataProvider();
        parentData.setLocators(parentLocators);
        /** Prepare child data. */
        Map<String, String> childLocators = new HashMap<>();
        parentLocators.put("parent2", "child2");
        parentLocators.put("child1", "child1");
        UnifiedDataProvider childData = new UnifiedDataProvider();
        childData.setLocators(childLocators);

        when(parserMock.load(anyString())).thenReturn(childData, childData, parentData);
        TestChildPanel panelMock = mock(TestChildPanel.class);
        parentLocators.putAll(childLocators);
        UnifiedDataProvider actual = UnifiedDataProviderFactory.getInstance(panelMock);
        assertThat("Merged locators is wrong.", actual.getLocators(), equalTo(parentLocators));
    }

    @Test
    public void getInstanceInheritPageTest() {
        /** Prepare parent data. */
        Map<String, String> parentLocators = new HashMap<>();
        parentLocators.put("parent1", "parent1");
        parentLocators.put("parent2", "parent2");
        UnifiedDataProvider parentData = new UnifiedDataProvider();
        parentData.setLocators(parentLocators);
        /** Prepare child data. */
        Map<String, String> childLocators = new HashMap<>();
        parentLocators.put("parent2", "child2");
        parentLocators.put("child1", "child1");
        UnifiedDataProvider childData = new UnifiedDataProvider();
        childData.setLocators(childLocators);

        when(parserMock.load(anyString())).thenReturn(childData, childData, parentData);
        ChildPage pageMock = mock(ChildPage.class);
        parentLocators.putAll(childLocators);
        UnifiedDataProvider actual = UnifiedDataProviderFactory.getInstance(pageMock);
        assertThat("Merged locators is wrong.", actual.getLocators(), equalTo(parentLocators));
    }

    @Test
    public void getInstanceFromCacheTest() {
        /** Get instance twice to invoke cache. */
        UnifiedDataProviderFactory.getInstance(new Object());
        UnifiedDataProviderFactory.getInstance(new Object());
        /** Two requests but only one load. */
        verify(parserMock, times(1)).load("Object");
    }
}
