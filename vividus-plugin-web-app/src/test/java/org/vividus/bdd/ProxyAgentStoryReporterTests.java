/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.bdd;

import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.StoryReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.spring.Configuration;
import org.vividus.proxy.IProxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProxyAgentStoryReporterTests
{
    private static final String NAME = "name";
    private static final String SOME_NAME = "someName";
    private static final String PROXY = "proxy";
    private static final String BLANK = "";
    private static final List<String> PROXY_META = Arrays.asList("proxy");
    private static final String TEST = "test";
    private static final String SCENARIO_TITLE = "scenario";

    @Mock
    private IProxy proxy;

    @Mock
    private IBddRunContext bddRunContext;

    @Mock
    private Configuration configuration;

    @Mock
    private StoryReporter next;

    @InjectMocks
    private ProxyAgentStoryReporter proxyAgentStoryReporter;

    // TODO for the final variant, create another branch, delete all the comments here

    // Created

//    @Test
//    void testBeforeStoryDryRun()
//    {
//        when(configuration.dryRun()).thenReturn(true);
//        RunningStory runningStory = new RunningStory();
//        Story story = mock(Story.class);
//        runningStory.setStory(story);
//        when(story.getName()).thenReturn("name");
//        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
//        proxyAgentStoryReporter.beforeStory(story, false);
//        verifyZeroInteractions(proxy);
//    }
//
//    @Test
//    void testBeforeStoryWithGivenStory()
//    {
//        RunningStory runningStory = new RunningStory();
//        Story story = mock(Story.class);
//        runningStory.setStory(story);
//        when(story.getName()).thenReturn(NAME);
//        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
//        proxyAgentStoryReporter.beforeStory(story, true);
//        verifyZeroInteractions(proxy);
//    }
//
//    @Test
//    void testBeforeStoryWithBeforeStory()
//    {
//        RunningStory runningStory = new RunningStory();
//        Story story = mock(Story.class);
//        runningStory.setStory(story);
//        when(story.getName()).thenReturn("BeforeStories");
//        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
//        proxyAgentStoryReporter.beforeStory(story, false);
//        verifyZeroInteractions(proxy);
//    }
//
//    @Test
//    void testBeforeStoryWithAfterStory()
//    {
//        RunningStory runningStory = new RunningStory();
//        Story story = mock(Story.class);
//        runningStory.setStory(story);
//        when(story.getName()).thenReturn("AfterStories");
//        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
//        proxyAgentStoryReporter.beforeStory(story, false);
//        verifyZeroInteractions(proxy);
//    }
//
//    @Test
//    void testBeforeStoryWithProxyDisabled()
//    {
//        RunningStory runningStory = new RunningStory();
//        Story story = mock(Story.class);
//        runningStory.setStory(story);
//        when(story.getName()).thenReturn(NAME);
//        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
//        proxyAgentStoryReporter.setProxyEnabled(false);
//        Meta meta = mock(Meta.class);
//        when(story.getMeta()).thenReturn(meta);
////        when(meta.getProperty(PROXY)).thenReturn(BLANK);
//        proxyAgentStoryReporter.beforeStory(story, false);
//        verifyZeroInteractions(proxy);
//
//    }

    // Copypasted

    @Test
    void testBeforeGivenStory()
    {
        RunningStory runningStory = mockRunningStoryWithName(SOME_NAME);
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), true);
        verifyZeroInteractions(proxy);
    }

    @Test
    void testBeforeStoryWhichIsBeforeStories()
    {
        RunningStory runningStory = mockRunningStoryWithName("BeforeStories");
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyZeroInteractions(proxy);
    }

    @Test
    void testBeforeStoryWhichIsAfterStories()
    {
        RunningStory runningStory =  mockRunningStoryWithName("AfterStories");
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyZeroInteractions(proxy);
    }

    @Test
    void testBeforeStoryProxy()
    {
        RunningStory runningStory =  mockRunningStoryWithName(SOME_NAME);
//        proxyAgentStoryReporter.setNext(next);
//        when(runningStory.getStory().getMeta()).thenReturn(new Meta(PROXY_META));
        proxyAgentStoryReporter.setProxyEnabled(true);
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verify(proxy).start();
    }

    @Test
    void testBeforeStoryProxyFromStoryMeta()
    {
        RunningStory runningStory =  mockRunningStoryWithName(SOME_NAME);
//        proxyAgentStoryReporter.setNext(next);
        when(runningStory.getStory().getMeta()).thenReturn(new Meta(PROXY_META));
        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verify(proxy).start();
    }

    @Test
    void testBeforeStoryDryRun()
    {
        when(configuration.dryRun()).thenReturn(true);
        Story story = mock(Story.class);
        when(story.getName()).thenReturn(SOME_NAME);
        RunningStory runningStory = runningStory(story);

        // TODO why is it here?
//        proxyAgentStoryReporter.setNext(next);
//        proxyAgentStoryReporter.setProxyEnabled(false);

        proxyAgentStoryReporter.beforeStory(runningStory.getStory(), false);
        verifyZeroInteractions(proxy);
    }

    @Test
    void testBeforeScenarioDryRun()
    {
        when(configuration.dryRun()).thenReturn(true);
        RunningStory runningStory = new RunningStory();
        Story story = mock(Story.class);
        runningStory.setStory(story);
        Scenario scenario = runningScenario(SCENARIO_TITLE, Collections.emptyList(), runningStory);
//        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
        proxyAgentStoryReporter.setNext(next);
        proxyAgentStoryReporter.beforeScenario(scenario);
        verifyZeroInteractions(proxy);
    }

    @Test
    void testBeforeScenarioStartProxyRecordingByProperty()
    {
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.beforeScenario(mock(Scenario.class));
        verify(proxy).clearRequestFilters();
        verify(proxy).startRecording();
    }

    @Test
    void testBeforeScenarioStartProxyRecordingByScenarioMeta()
    {
        Story story = mock(Story.class);
        when(story.getMeta()).thenReturn(new Meta());
        RunningStory runningStory = runningStory(story);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        Scenario scenario = runningScenario(SCENARIO_TITLE, PROXY_META, runningStory);
        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);

        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).clearRequestFilters();
        verify(proxy).startRecording();
    }

    // TODO ? add testBeforeScenarioStartProxyRecordingByStoryMeta

    @Test
    void testBeforeScenarioNoProxyRecordingStart()
    {
        Scenario scenario = mockRunningScenarioWithMeta(SCENARIO_TITLE, Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
        proxyAgentStoryReporter.setNext(next);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);

        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy, never()).clearRequestFilters();
        verify(proxy, never()).startRecording();
    }

    @Test
    void testBeforeScenarioStartProxy()
    {
        Scenario scenario = mockRunningScenarioWithMeta(SCENARIO_TITLE, Collections.emptyList(), PROXY_META);
        // redundant throughout? since mocks return false by default
//        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
//        when(proxy.isStarted()).thenReturn(Boolean.FALSE);
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).start();
    }

    @Test
    void testBeforeScenarioNoProxyStart()
    {
        Scenario scenario = mockRunningScenarioWithMeta(SCENARIO_TITLE, Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy, never()).start();
    }

    @Test
    void testBeforeScenarioStartProxyAndRecording()
    {
        Scenario scenario = mockRunningScenarioWithMeta(SCENARIO_TITLE, PROXY_META, Collections.emptyList());
//        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
//        proxyAgentStoryReporter.setNext(next);
        when(proxy.isStarted()).thenAnswer(new Answer<Boolean>()
        {
            private int count;

            @Override
            public Boolean answer(InvocationOnMock invocation)
            {
                return ++count != 1;
            }
        });
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).start();
        verify(proxy).clearRequestFilters();
        verify(proxy).startRecording();
    }

    @Test
    void testBeforeScenarioStartProxyEnabledInStoryMeta()
    {
        Scenario scenario = mockRunningScenarioWithMeta(SCENARIO_TITLE, PROXY_META, Collections.emptyList());
//        when(proxy.isStarted()).thenReturn(Boolean.FALSE);
        proxyAgentStoryReporter.beforeScenario(scenario);
        verify(proxy).start();
    }

    @Test
    void testAfterScenarioWhenNoProxyIsStarted()
    {
//        Story story = mock(Story.class);
//        RunningStory runningStory = new RunningStory();
//        runningStory.setStory(story);
//        Scenario scenario = new Scenario(SCENARIO_TITLE, new Meta(Collections.emptyList()));
//        RunningScenario runningScenario = new RunningScenario();
//        runningScenario.setScenario(scenario);
//        runningStory.setRunningScenario(runningScenario);
//        when(proxy.isStarted()).thenReturn(Boolean.FALSE);
        proxyAgentStoryReporter.afterScenario();
        verifyProxyInactivity();
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedButNoCleanUpIsNeeded()
    {
        mockRunningScenarioWithMeta(SCENARIO_TITLE, Collections.emptyList(), Collections.emptyList());
        proxyAgentStoryReporter.setProxyEnabled(true);
//        proxyAgentStoryReporter.setProxyRecordingEnabled(false);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterScenario();
        verifyProxyInactivity();
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedAndStopRecordingIsNeededButProxyStopIsNot()
    {
        mockRunningScenario(SCENARIO_TITLE,  Collections.emptyList());
//        mockRunningScenarioWithMeta(SCENARIO_TITLE, Collections.emptyList(), Collections.emptyList());
//        proxyAgentStoryReporter.setProxyEnabled(false);
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterScenario();
        verify(proxy).stopRecording();
        verifyNoMoreInteractions(proxy);
    }

    @Test
    void testAfterScenarioWhenProxyIsStartedAndStopRecordingAndStopProxyAreNeeded()
    {
        mockRunningScenario(SCENARIO_TITLE,  PROXY_META);
        proxyAgentStoryReporter.setProxyRecordingEnabled(true);
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterScenario();
        verify(proxy).stopRecording();
        verify(proxy).stop();
    }

    @Test
    void testAfterGivenStory()
    {
        proxyAgentStoryReporter.afterStory(true);
        verifyZeroInteractions(proxy);
    }

    @Test
    void testAfterStoryWhenNoCleanUpIsNeeded()
    {
        proxyAgentStoryReporter.afterStory(false);
        verifyProxyInactivity();
    }

    @Test
    void testAfterStoryWhenProxyIsStarted()
    {
        when(proxy.isStarted()).thenReturn(Boolean.TRUE);
        proxyAgentStoryReporter.afterStory(false);
        verify(proxy).stop();
    }

    // TODO refactor the utilities

    private RunningStory runningStory(Story story)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        when(bddRunContext.getRunningStory()).thenReturn(runningStory);
        return runningStory;
    }

    private RunningStory mockRunningStoryWithName(String storyName)
    {
        Story story = mock(Story.class);
        when(story.getName()).thenReturn(storyName);
//        when(story.getMeta()).thenReturn(new Meta(storyMeta));
        return runningStory(story);
    }

    private Scenario runningScenario(String scenarioTitle, List<String> scenarioMeta, RunningStory runningStory)
    {
        Scenario scenario = new Scenario(scenarioTitle, new Meta(scenarioMeta));
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        runningStory.setRunningScenario(runningScenario);
        return scenario;
    }

    private Scenario mockRunningScenarioWithMeta(String scenarioTitle, List<String> storyMeta,
                                                 List<String> scenarioMeta)
    {
        RunningStory runningStory = mockRunningStoryWithMeta(storyMeta);
        return  runningScenario(scenarioTitle, scenarioMeta, runningStory);
    }

    private RunningStory mockRunningStoryWithMeta(List<String> storyMeta)
    {
        Story story = mock(Story.class);
        when(story.getMeta()).thenReturn(new Meta(storyMeta));
        return runningStory(story);
    }

    private void verifyProxyInactivity()
    {
        verify(proxy).isStarted();
        verifyNoMoreInteractions(proxy);
    }

    // TODO clarify in naming why 2 utility mocking method duplicates are needed

    private RunningStory mockRunningStory()
    {
        Story story = mock(Story.class);
        return runningStory(story);
    }

    private void mockRunningScenario(String scenarioTitle, List<String> scenarioMeta)
    {
        RunningStory runningStory = mockRunningStory();
        runningScenario(scenarioTitle, scenarioMeta, runningStory);
    }
}
