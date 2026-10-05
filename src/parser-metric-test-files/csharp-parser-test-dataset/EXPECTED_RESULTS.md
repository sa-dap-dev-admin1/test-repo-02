# C# parser test dataset: expected results

Strict mode = Roslyn with `-langversion:` set to the Version column (`<LangVersion>` in a project), `-unsafe` enabled.

Validation: C# 7.0-12 rows were compiled with Roslyn 4.8 (.NET SDK 8.0.131) at that exact language version and RUN on .NET 8. Several were also confirmed to FAIL at the previous version (7.0/7.3/8.0). C# 13 and 14 cannot be compiled with that toolchain: they were written against the specs, and the C# 12 compiler was confirmed to fail ONLY at the new-syntax sites. Verify them on .NET 9 / .NET 10.

Fill in the "Parser result" column when you run your parser. "Pass" means the parser accepts the file in the mode for its version. "Fail" means a correct parser must REJECT it with a useful error, not crash or silently mis-parse.

| File | Version | Expected (strict) | Parser result | Validated with / notes |
|---|---|---|---|---|
| `csharp10/ExtendedPropertyPatternsInterpolationHandlers.cs` | C# 10 | Pass | | Roslyn 4.8, ran |
| `csharp10/FileScopedNamespaceGlobalUsing.cs` | C# 10 | Pass | | Roslyn 4.8, ran |
| `csharp10/RecordStructsLambdaImprovements.cs` | C# 10 | Pass | | Roslyn 4.8, ran |
| `csharp11/ListPatterns.cs` | C# 11 | Pass | | Roslyn 4.8, ran |
| `csharp11/RawStringLiterals.cs` | C# 11 | Pass | | Roslyn 4.8, ran |
| `csharp11/RequiredStaticAbstractGenericMath.cs` | C# 11 | Pass | | Roslyn 4.8, ran |
| `csharp12/AliasAnyTypeDefaultLambdaParamsInlineArrays.cs` | C# 12 | Pass | | Roslyn 4.8 `-unsafe`, ran. Needs AllowUnsafeBlocks. |
| `csharp12/CollectionExpressions.cs` | C# 12 | Pass | | Roslyn 4.8, ran |
| `csharp12/PrimaryConstructors.cs` | C# 12 | Pass | | Roslyn 4.8, ran |
| `csharp13/ParamsCollectionsLockEscape.cs` | C# 13 | Pass (13) | | UNVERIFIED - needs .NET 9. C# 12 compiler fails only at `\e` escapes (CS1009). |
| `csharp13/PartialPropertiesRefStructInterfaces.cs` | C# 13 | Pass (13) | | UNVERIFIED - needs .NET 9. C# 12 compiler fails at the partial property / `allows ref struct` sites. |
| `csharp14/ExtensionMembersFieldKeyword.cs` | C# 14 | Pass (14) | | UNVERIFIED - needs .NET 10. C# 12 compiler fails at `extension(...)` blocks. |
| `csharp14/NullConditionalAssignmentSpans.cs` | C# 14 | Pass (14) | | UNVERIFIED - needs .NET 10. C# 12 compiler fails at partial events and compound operator declarations. |
| `csharp7/Csharp7PointReleases.cs` | C# 7.0-7.3 | Pass (7.3) / Fail (7.0) | | Roslyn, both directions checked. Fails 7.0 with CS8107 (leading digit separator). |
| `csharp7/OutVarsLocalFunctionsRefReturns.cs` | C# 7.0-7.3 | Pass (7.0+) | | Roslyn at 7.0 and 7.3, ran |
| `csharp7/PatternMatchingIsSwitch.cs` | C# 7.0-7.3 | Pass (7.0+) | | Roslyn at 7.0 and 7.3, ran |
| `csharp7/TuplesDeconstruction.cs` | C# 7.0-7.3 | Pass (7.0+) | | Roslyn at 7.0 and 7.3, ran |
| `csharp8/AsyncStreamsUsingDeclarations.cs` | C# 8 | Pass | | Roslyn 4.8, ran |
| `csharp8/IndicesRangesDefaultInterface.cs` | C# 8 | Pass (8) / Fail (7.3) | | Roslyn, both directions checked. Default interface methods need .NET Core 3.0+ runtime. |
| `csharp8/NullableReferenceTypes.cs` | C# 8 | Pass | | Roslyn 4.8, ran |
| `csharp8/SwitchExpressionsRecursivePatterns.cs` | C# 8 | Pass | | Roslyn 4.8, ran |
| `csharp9/FunctionPointersNativeInts.cs` | C# 9 | Pass | | Roslyn 4.8 `-unsafe`, ran. Needs AllowUnsafeBlocks. |
| `csharp9/InitOnlyTargetTypedNew.cs` | C# 9 | Pass | | Roslyn 4.8, ran |
| `csharp9/PatternCombinators.cs` | C# 9 | Pass | | Roslyn 4.8, ran |
| `csharp9/Records.cs` | C# 9 | Pass (9) / Fail (8) | | Roslyn, both directions checked |
| `csharp9/TopLevelStatements.cs` | C# 9 | Pass (9) / Fail (8) | | Roslyn, both directions checked. Only ONE file per project may contain top-level statements. Compile it alone. |
