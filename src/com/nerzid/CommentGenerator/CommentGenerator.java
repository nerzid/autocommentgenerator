/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nerzid.CommentGenerator;

import com.sun.source.doctree.CommentTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.*;

public class CommentGenerator implements CodeGenerator {

    JTextComponent textComp;
    ArrayList<String> methodNames;
    ArrayList<String> checkedMethodNames;
    boolean isOkeyBtnClicked = false;

    /**
     *
     * @param context containing JTextComponent and possibly other items
     * registered by {@link CodeGeneratorContextProvider}
     */
    private CommentGenerator(Lookup context) { // Good practice is not to save Lookup outside ctor
        textComp = context.lookup(JTextComponent.class);
    }

    @MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
    public static class Factory implements CodeGenerator.Factory {

        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new CommentGenerator(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    public String getDisplayName() {
        return "Auto-generated Comments";
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    public void invoke() {
        JOptionPane.showMessageDialog(null, "Hello there");

        methodNames = new ArrayList<>();

        try {
            Document doc = textComp.getDocument();
            JavaSource javaSource = JavaSource.forDocument(doc);
            CancellableTask task;
            task = new CancellableTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {

                    workingCopy.toPhase(Phase.RESOLVED);
                    CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    TreeMaker make = workingCopy.getTreeMaker();
                    for (Tree typeDecl : cut.getTypeDecls()) {
                        if (Tree.Kind.CLASS == typeDecl.getKind()) {
                            ClassTree clazz = (ClassTree) typeDecl;

                            Comment ccc = Comment.create("comment for new method");

                            ModifiersTree methodModifiers
                                    = make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                                            Collections.<AnnotationTree>emptyList());
                            VariableTree parameter
                                    = make.Variable(make.Modifiers(Collections.<Modifier>singleton(Modifier.FINAL),
                                            Collections.<AnnotationTree>emptyList()),
                                            "arg0",
                                            make.Identifier("Object"),
                                            null);
                            TypeElement element = workingCopy.getElements().getTypeElement("java.io.IOException");
                            ExpressionTree throwsClause = make.QualIdent(element);
                            MethodTree newMethod
                                    = make.Method(methodModifiers,
                                            "writeExternal",
                                            make.PrimitiveType(TypeKind.VOID),
                                            Collections.<TypeParameterTree>emptyList(),
                                            Collections.singletonList(parameter),
                                            Collections.<ExpressionTree>singletonList(throwsClause),
                                            "{ throw new UnsupportedOperationException(\"Not supported yet.\") }",
                                            null);

                            ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);

                            make.addComment(modifiedClazz, Comment.create("Buralar benim classimin topragam"), true);
                            make.addComment(newMethod, ccc, true);
                            make.addComment(throwsClause, Comment.create("Burası expression tree"), true);
                            make.addComment(parameter, Comment.create("burası variabletree yani parameter denilen kısım"), true);

                            
                            
                            for (Tree t : modifiedClazz.getMembers())//Class' members, basically they are methods.
                            {
                                //JOptionPane.showMessageDialog(null, "Kind: " + t.getKind() + " asd: " + t.toString());
                                MethodTree tk = (MethodTree) t;
                                methodNames.add(tk.getName().toString());
                                for (Tree tkt : tk.getBody().getStatements()) {
                                    //JOptionPane.showMessageDialog(null, "Kind: " + tkt.getKind());
                                }
                            }

                            prepareMethodChooser(clazz.getSimpleName().toString(), methodNames, workingCopy);

                            //CommentHandlerService chs = new CommentHandlerService();
                            
                            for (Tree t : modifiedClazz.getMembers())//Class' members, basically they are methods.
                            {
                                System.out.println("KINDDDDDD: " + t.getKind());
                                JOptionPane.showMessageDialog(null, "Kind: " + t.getKind() + " asd: " + t.toString());
                                MethodTree tk = (MethodTree) t;
                                String methodName = tk.getName().toString();
                                
                                
                               
                                if (checkedMethodNames.contains(methodName)) {
                                    

                                }
                                make.addComment(tk, Comment.create(Comment.Style.JAVADOC,"This comment for the method named: " + methodName), true);
                            }

                            workingCopy.rewrite(clazz, modifiedClazz);
                        }
//                        JOptionPane.showMessageDialog(null, "TypeDec: " + typeDecl.toString() + " , Kind: " + typeDecl.getKind());

                    }

                }

                public void cancel() {
                }
            };
            ModificationResult result = javaSource.runModificationTask(task);
            result.commit();

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private void prepareMethodChooser(String className, ArrayList<String> methodNames, WorkingCopy workingCopy) {
        final JFrame frame = new JFrame("Select Methods");
        frame.getContentPane().setLayout(new BorderLayout());

        DefaultMutableTreeNode top = new DefaultMutableTreeNode(className, true);

        for (String mName : methodNames) {
            top.add(new DefaultMutableTreeNode(mName));
        }

        DefaultTreeModel dtm = new DefaultTreeModel(top);

        final JCheckBoxTree cbt = new JCheckBoxTree();
        cbt.setModel(dtm);
        dtm.reload(top);
        checkedMethodNames = new ArrayList<>();
        cbt.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
            public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {

                System.out.println("event");
                TreePath[] paths = cbt.getCheckedPaths();
                for (TreePath tp : paths) {
                    for (Object pathPart : tp.getPath()) {
                        //System.out.print(pathPart + ",");
                    }
                    //System.out.println();
                }
            }
        });
        JButton okeyBtn = new JButton("Okey");
        okeyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath[] paths = cbt.getCheckedPaths();
                for (TreePath tp : paths) {
                    for (int i = 1; i < tp.getPath().length; i++) {
                        System.out.println(tp.getPath()[i].toString());
                        checkedMethodNames.add(tp.getPath()[i].toString());
                        isOkeyBtnClicked = true;
                    }
                }
                frame.dispose();
                System.out.println("Methods has been choosen.");
            }
        });

        frame.getContentPane().add(cbt);
        frame.getContentPane().add(okeyBtn, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(300, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

}
