// Copyright 2016 Proyectos y Sistemas de Mantenimiento SL (eProsima).
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.eprosima.microxrcedds.idl.grammar;

import java.util.ArrayList;
import java.util.Stack;

import com.eprosima.idl.parser.tree.Interface;
import com.eprosima.idl.parser.tree.TypeDeclaration;
import com.eprosima.idl.parser.tree.Annotation;
import com.eprosima.idl.parser.typecode.Kind;
import com.eprosima.idl.generator.manager.TemplateManager;
import com.eprosima.microxrcedds.idl.parser.typecode.StructTypeCode;
import com.eprosima.microxrcedds.idl.parser.typecode.SequenceTypeCode;

public class Context extends com.eprosima.idl.context.Context implements com.eprosima.microxrcedds.idl.context.Context
{
    public Context(TemplateManager tmanager, String filename, String file, ArrayList<String> includePaths, boolean subscribercode, boolean publishercode, String default_unbounded_max_size)
    {
        super(tmanager, file, includePaths, false);
        m_fileNameUpper = filename.toUpperCase();
        m_subscribercode = subscribercode;
        m_publishercode = publishercode;
        m_randomGenNames = new Stack<String>();
        m_default_unbounded_max_size = default_unbounded_max_size;

    }

    public void setTypelimitation(String lt)
    {
        m_typelimitation = lt;
    }

    public String getTypelimitation()
    {
        return m_typelimitation;
    }

    public String getCScope()
    {
        String[] scopeTemp = getScope().split("::");
        String scope = String.join("_", scopeTemp).replace("_Constants", "");
        return scope;
    }

    @Override
    public StructTypeCode createStructTypeCode(String name)
    {
        return new StructTypeCode(getScope(), name);
    }

    private String m_default_unbounded_max_size;
    @Override
    public SequenceTypeCode createSequenceTypeCode(
            String maxsize)
    {
        return new SequenceTypeCode(maxsize, evaluate_literal(maxsize), m_default_unbounded_max_size);
    }

    @Override
    public void addTypeDeclaration(TypeDeclaration typedecl)
    {
        super.addTypeDeclaration(typedecl);

        if(typedecl.getTypeCode().getKind() == Kind.KIND_STRUCT && typedecl.isInScope())
        {
            Annotation topicann = typedecl.getAnnotations().get("Topic");

            if(topicann != null && topicann.getValue("value").equalsIgnoreCase("false"))
            {
                StructTypeCode structtypecode = (StructTypeCode)typedecl.getTypeCode();
                structtypecode.setIsTopic(false);
            }
            else
                m_lastStructure = typedecl;
        }
    }

    public boolean isClient()
    {
        return m_subscribercode;
    }

    public boolean isServer()
    {
        return m_publishercode;
    }

    // TODO Para stringtemplate TopicsPlugin de nuestros tipos DDS.
    public String getNewRandomName()
    {
        String name = "type_" + ++m_randomGenName;
        m_randomGenNames.push(name);
        return name;
    }

    public String getNewLoopVarName()
    {
        m_loopVarName = 'a';
        return Character.toString(m_loopVarName);
    }

    public String getNextLoopVarName()
    {
        return Character.toString(++m_loopVarName);
    }

    // TODO Para stringtemplate TopicsPlugin de nuestros tipos DDS.
    public String getLastRandomName()
    {
        return m_randomGenNames.pop();
    }

    /*** Functions inherated from FastCDR Context ***/

    @Override
    public boolean isPrintexception()
    {
        return false;
    }

    @Override
    public boolean isPrintoperation()
    {
        return false;
    }

    public String getProduct()
    {
        return "microxrcedds";
    }

    public String getNamespace()
    {
        return "microxrcedds";
    }

    public boolean isCdr()
    {
        return true;
    }

    public boolean isAnyCdr()
    {
        return true;
    }

    /*** End ***/

    //// Java block ////
    public void setPackage(String pack)
    {
        if(pack != null && !pack.isEmpty())
        {
            m_package = pack + ".";
            m_onlypackage = pack;
            m_packageDir = m_package.replace('.', '/');
        }
    }

    public boolean isIsPackageEmpty()
    {
        return m_package.isEmpty();
    }

    public String getPackage()
    {
        return m_package;
    }

    public String getOnlyPackage()
    {
        return m_onlypackage;
    }

    public String getPackageDir()
    {
        return m_packageDir;
    }

    public String getPackageUnder()
    {
        return m_package.replace('.', '_');
    }
    //// End Java block ////

    private String m_typelimitation = null;

    // TODO Lleva la cuenta de generacion de nuevos nombres.
    private int m_randomGenName = 0;
    private Stack<String> m_randomGenNames = null;
    // TODO Lleva la cuenta del nombre de variables para bucles anidados.
    private char m_loopVarName = 'a';

    // Stores if the user will generate the client source.
    private boolean m_subscribercode = true;
    // Stores if the user will generate the server source.
    private boolean m_publishercode = true;

    private TypeDeclaration m_lastStructure = null;

    public String getM_lastStructureTopicDataTypeName() {
        String name = new String("");

        if(m_lastStructure!=null)
        {
            if(m_lastStructure.getParent() instanceof Interface)
            {
                name = name + ((Interface)m_lastStructure.getParent()).getScopedname() + "_" + m_lastStructure.getName();
            }
            else
                name = m_lastStructure.getScopedname();
        }
        return name;
    }

    public String getM_lastStructureScopedName(){
        if(m_lastStructure!=null)
        {
            return m_lastStructure.getScopedname();
        }
        return null;
    }

    public boolean isThereIsStructure()
    {
        if(m_lastStructure != null)
            return true;
        return false;
    }

    public TypeDeclaration getLastStructure()
    {
        return m_lastStructure;
    }

    public boolean existsLastStructure()
    {
        if(m_lastStructure != null)
            return true;
        return false;
    }

    private String m_fileNameUpper = null;

    public void setFilename(String filename)
    {
        super.setFilename(filename);
        m_fileNameUpper = filename.toUpperCase();
    }
    public String getFileNameUpper()
    {
        return m_fileNameUpper;
    }

    public String getJniFilename()
    {
        return getFilename().replace("_", "_1");
    }

    public String getHeaderGuardName ()
    {
        if (m_lastStructure != null)
        {
            if (m_lastStructure.getHasScope())
            {
                return m_lastStructure.getScope().replaceAll("::", "_").toUpperCase() +
                    "_" + m_fileNameUpper.replaceAll("\\.", "_");
            }
        }
        return m_fileNameUpper;
    }

    //// Java block ////
    // Java package name.
    private String m_package = "";
    private String m_onlypackage = "";
    // Java package dir.
    private String m_packageDir = "";
    //// End Java block

}
