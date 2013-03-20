/*
 * Copyright 2012 Seitenbau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seitenbau.jenkins.plugins.dynamicparameter.scriptler;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.StringParameterValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.scriptler.config.Script;
import org.jvnet.localizer.ResourceBundleHolder;
import org.kohsuke.stapler.DataBoundConstructor;

import com.seitenbau.jenkins.plugins.dynamicparameter.ChoiceParameterDefinition;
import com.seitenbau.jenkins.plugins.dynamicparameter.util.JenkinsUtils;

/** Choice parameter, with dynamically generated list of values. */
public class ScriptlerChoiceParameterDefinition extends ScriptlerParameterDefinition
{
  /** Serial version UID. */
  private static final long serialVersionUID = -5751222508337683456L;

  private final boolean readonlyInputField;

  private String choiceType;

  /**
   * Constructor.
   * @param name parameter name
   * @param description parameter description
   * @param uuid identifier (optional)
   * @param scriptlerScriptId Scriptler script id
   * @param parameters script parameters
   * @param remote execute the script on a remote node
   * @param choiceType type of the choice (single, multi, etc.) to display
   */
  @DataBoundConstructor
  public ScriptlerChoiceParameterDefinition(String name, String description, String uuid,
      String scriptlerScriptId, ScriptParameter[] parameters, boolean remote, boolean readonlyInputField, String choiceType)
  {
    super(name, description, uuid, scriptlerScriptId, parameters, remote);
    this.readonlyInputField = readonlyInputField;
    this.choiceType = choiceType;
  }

  /**
   * Return default parameter value - used by trigger mechanism.
   */
  @Override
  public ParameterValue getDefaultParameterValue() {

    Object firstElement = null;
    // Ensure list does exist and is not empty! Otherwise return null
    if (getChoices() != null && getChoices().size() > 0) {
        firstElement = getChoices().get(0);
    }

    StringParameterValue stringParameterValue = new StringParameterValue(getName(), ObjectUtils.toString(firstElement, null));
    return stringParameterValue;
  }

  /**
   * Get the possible choices, generated by the script.
   * @return list of values if the script returns a non-null list;
   *         {@link Collections#EMPTY_LIST}, otherwise
   */
  public final List<Object> getChoices()
  {
    return getScriptResultAsList();
  }

  /**
   * Check if the given parameter value is within the list of possible values.
   * @param parameter parameter value to check
   * @throws IllegalArgumentException if the value is not a valid choice
   * @return the value if it is valid
   */
  @Override
  protected StringParameterValue checkParameterValue(StringParameterValue parameter)
  {
    String actualValue = ObjectUtils.toString(parameter.value);

    if (checkActualValue(actualValue))
    {
        return parameter;
    }

    throw new IllegalArgumentException("Illegal choice: " + actualValue);
  }

  private boolean checkActualValue(String actualValue)
  {
    List<String> values = Arrays.asList(StringUtils.split(actualValue, ","));
    List<Object> choices = getChoices();
    List<String> stringChoices = new ArrayList<String>();

    for (Object choice : choices)
    {
      stringChoices.add(ObjectUtils.toString(choice));
    }

      boolean result = stringChoices.containsAll(values);
      return result;
  }

  public boolean isReadonlyInputField()
  {
    return readonlyInputField;
  }

  public void setChoiceType(String choiceType)
  {
    this.choiceType = choiceType;
  }

  public String getChoiceType()
  {
    return choiceType;
  }

  public int getVisibleItemCount()
  {
    final int choicesCount = getChoices().size();

    if (choicesCount < ChoiceParameterDefinition.DEFAULT_MAX_VISIBLE_ITEM_COUNT)
    {
      return choicesCount;
    }

    return ChoiceParameterDefinition.DEFAULT_MAX_VISIBLE_ITEM_COUNT;
  }

  /** Parameter descriptor. */
  @Extension
  public static final class DescriptorImpl extends ParameterDescriptor
  {
    private static final String DISPLAY_NAME = "DisplayName";

    @Override
    public final String getDisplayName()
    {
      return ResourceBundleHolder.get(ScriptlerChoiceParameterDefinition.class)
          .format(DISPLAY_NAME);
    }

    public Set<Script> getScripts()
    {
      return JenkinsUtils.getAllScriptlerScripts();
    }
  }
}
