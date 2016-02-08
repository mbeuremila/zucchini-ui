package io.testscucumber.backend.reportconverter.converter;

import com.google.common.base.Strings;
import io.testscucumber.backend.feature.domain.Feature;
import io.testscucumber.backend.reportconverter.report.ReportAroundAction;
import io.testscucumber.backend.reportconverter.report.ReportBackground;
import io.testscucumber.backend.reportconverter.report.ReportComment;
import io.testscucumber.backend.reportconverter.report.ReportScenario;
import io.testscucumber.backend.reportconverter.report.ReportStep;
import io.testscucumber.backend.reportconverter.report.TableRow;
import io.testscucumber.backend.reportconverter.report.Tag;
import io.testscucumber.backend.scenario.domain.AroundActionBuilder;
import io.testscucumber.backend.scenario.domain.BackgroundBuilder;
import io.testscucumber.backend.scenario.domain.ScenarioBuilder;
import io.testscucumber.backend.scenario.domain.StepBuilder;
import io.testscucumber.backend.scenario.domain.StepStatus;
import io.testscucumber.backend.shared.domain.Argument;
import io.testscucumber.backend.shared.domain.BasicInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
class ReportScenarioConverter {

    public ScenarioBuilder createScenarioBuilder(final Feature parentFeature, final ReportScenario reportScenario) {

        final BasicInfo scenarioInfo = new BasicInfo(
            ConversionUtils.trimString(reportScenario.getKeyword()),
            ConversionUtils.trimString(reportScenario.getName())
        );

        final Set<String> scenarioTags = reportScenario.getTags().stream()
            .map(Tag::getName)
            .map(ConversionUtils::stripAtSign)
            .collect(Collectors.toSet());

        final String comment = convertComment(reportScenario.getComments());

        final ScenarioBuilder scenarioBuilder = new ScenarioBuilder()
            .withTestRunId(parentFeature.getTestRunId())
            .withFeatureId(parentFeature.getId())
            .withScenarioKey(ConversionUtils.stringToSha1Sum(reportScenario.getId()))
            .withInfo(scenarioInfo)
            .withTags(scenarioTags)
            .withExtraTags(parentFeature.getTags())
            .withComment(comment);

        for (final ReportStep reportStep : reportScenario.getSteps()) {
            scenarioBuilder.addStep(b -> buildStep(reportStep, b));
        }

        for (final ReportAroundAction reportAroundAction : reportScenario.getBeforeActions()) {
            scenarioBuilder.addBeforeAction(b -> buildAroundAction(reportAroundAction, b));
        }

        for (final ReportAroundAction reportAroundAction : reportScenario.getAfterActions()) {
            scenarioBuilder.addAfterAction(b -> buildAroundAction(reportAroundAction, b));
        }

        return scenarioBuilder;
    }

    public Consumer<BackgroundBuilder> createBackgroundBuilderConsumer(final ReportBackground reportBackground) {
        return backgroundBuilder -> {

            final BasicInfo backgroundInfo = new BasicInfo(
                ConversionUtils.trimString(reportBackground.getKeyword()),
                ConversionUtils.trimString(reportBackground.getName())
            );

            backgroundBuilder.withInfo(backgroundInfo);

            for (final ReportStep reportStep : reportBackground.getSteps()) {
                backgroundBuilder.addStep(b -> buildStep(reportStep, b));
            }
        };
    }

    private static void buildStep(final ReportStep reportStep, final StepBuilder stepBuilder) {

        final List<Argument> arguments = reportStep.getMatch().getArguments().stream()
            .filter(a -> !Strings.isNullOrEmpty(a.getValue()))
            .map(reportArg -> new Argument(reportArg.getOffset(), reportArg.getValue()))
            .collect(Collectors.toList());

        final BasicInfo stepInfo = new BasicInfo(
            ConversionUtils.trimString(reportStep.getKeyword()),
            ConversionUtils.trimString(reportStep.getName()),
            arguments
        );

        final String stepComment = convertComment(reportStep.getComments());

        final String[][] table = convertTable(reportStep.getTableRows());

        stepBuilder
            .withErrorMessage(reportStep.getResult().getErrorMessage())
            .withStatus(convertStepStatus(reportStep.getResult().getStatus()))
            .withInfo(stepInfo)
            .withComment(stepComment)
            .withTable(table);
    }

    private static void buildAroundAction(final ReportAroundAction reportAroundAction, final AroundActionBuilder aroundActionBuilder) {
        aroundActionBuilder
            .withErrorMessage(reportAroundAction.getResult().getErrorMessage())
            .withStatus(convertStepStatus(reportAroundAction.getResult().getStatus()));
    }

    private static String[][] convertTable(final List<TableRow> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        final String[][] table = new String[source.size()][];

        int i = 0;
        for (final TableRow sourceRow : source) {
            final String[] targetRow = new String[sourceRow.getCells().size()];
            int j = 0;
            for (final String cell : sourceRow.getCells()) {
                targetRow[j] = cell;
                j++;
            }
            table[i] = targetRow;
            i++;
        }

        return table;
    }

    private static String convertComment(final List<ReportComment> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        return source.stream()
            .map(ReportComment::getValue)
            .collect(Collectors.joining("\n"));
    }

    private static StepStatus convertStepStatus(final String source) {
        if (source == null) {
            return null;
        }
        return StepStatus.valueOf(source.toUpperCase());
    }

}