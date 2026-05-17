/*
 * This file is part of the Sindri package.
 *
 * (c) Melech Mizrachi <melechmizrachi@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.sindri.archunit;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "io.sindri", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    public static final ArchRule data_records_should_reside_in_data_packages =
            classes()
                    .that().areRecords()
                    .should().resideInAPackage("..data..")
                    .because("All data records should reside in an appropriate data namespace");

    @ArchTest
    public static final ArchRule data_packages_should_only_contain_records =
            classes()
                    .that().resideInAPackage("..data..")
                    .and().resideOutsideOfPackage("..data.contract..")
                    .should().beRecords()
                    .because("All classes in a data namespace must be records");

    @ArchTest
    public static final ArchRule generators_should_not_depend_on_providers =
            noClasses()
                    .that().resideInAPackage("io.sindri.generator..")
                    .should().dependOnClassesThat().resideInAPackage("io.sindri.provider..")
                    .because("Generators must not depend on application-level providers");

    @ArchTest
    public static final ArchRule ast_readers_should_not_depend_on_generators =
            noClasses()
                    .that().resideInAPackage("io.sindri.ast..")
                    .should().dependOnClassesThat().resideInAPackage("io.sindri.generator..")
                    .because("AST readers must not depend on generators — data flows one way");
}
