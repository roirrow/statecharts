/**
 * <copyright>
 * </copyright>
 *
 */
package org.eclipselabs.mscript.language.ast;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Foreach Statement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipselabs.mscript.language.ast.ForeachStatement#getElementName <em>Element Name</em>}</li>
 *   <li>{@link org.eclipselabs.mscript.language.ast.ForeachStatement#getCollectionName <em>Collection Name</em>}</li>
 *   <li>{@link org.eclipselabs.mscript.language.ast.ForeachStatement#getBody <em>Body</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipselabs.mscript.language.ast.AstPackage#getForeachStatement()
 * @model
 * @generated
 */
public interface ForeachStatement extends Statement
{
  /**
   * Returns the value of the '<em><b>Element Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Element Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Element Name</em>' attribute.
   * @see #setElementName(String)
   * @see org.eclipselabs.mscript.language.ast.AstPackage#getForeachStatement_ElementName()
   * @model
   * @generated
   */
  String getElementName();

  /**
   * Sets the value of the '{@link org.eclipselabs.mscript.language.ast.ForeachStatement#getElementName <em>Element Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Element Name</em>' attribute.
   * @see #getElementName()
   * @generated
   */
  void setElementName(String value);

  /**
   * Returns the value of the '<em><b>Collection Name</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Collection Name</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Collection Name</em>' containment reference.
   * @see #setCollectionName(QualifiedName)
   * @see org.eclipselabs.mscript.language.ast.AstPackage#getForeachStatement_CollectionName()
   * @model containment="true"
   * @generated
   */
  QualifiedName getCollectionName();

  /**
   * Sets the value of the '{@link org.eclipselabs.mscript.language.ast.ForeachStatement#getCollectionName <em>Collection Name</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Collection Name</em>' containment reference.
   * @see #getCollectionName()
   * @generated
   */
  void setCollectionName(QualifiedName value);

  /**
   * Returns the value of the '<em><b>Body</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Body</em>' containment reference isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Body</em>' containment reference.
   * @see #setBody(Block)
   * @see org.eclipselabs.mscript.language.ast.AstPackage#getForeachStatement_Body()
   * @model containment="true"
   * @generated
   */
  Block getBody();

  /**
   * Sets the value of the '{@link org.eclipselabs.mscript.language.ast.ForeachStatement#getBody <em>Body</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Body</em>' containment reference.
   * @see #getBody()
   * @generated
   */
  void setBody(Block value);

} // ForeachStatement
