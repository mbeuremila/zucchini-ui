import React from 'react';

import TagList from '../../ui/components/TagList';
import HistoryFilterContainer from '../../filters/components/HistoryFilterContainer';
import ScenarioHistoryTableContainer from './ScenarioHistoryTableContainer';
import CommentListContainer from './CommentListContainer';
import ScenarioChangeTable from './ScenarioChangeTable';


export default class ScenarioPage extends React.Component {

  componentDidMount() {
    this.loadScenarioIfNeeded();
  }

  componentDidUpdate(prevProps) {
    this.loadScenarioIfNeeded(prevProps);
  }

  render() {
    const { scenario } = this.props;

    let tagList = null;
    if (scenario.tags.length > 0) {
      tagList = (
        <p>
          <b>Tags :</b>{' '}
          <TagList tags={scenario.tags} />
        </p>
      );
    }

    return (
      <div>
        <h1><b>{scenario.info.keyword}</b> {scenario.info.name}</h1>
        {tagList}

        <hr />

        <h2>Étapes du scénario</h2>
        <p>TODO</p>

        <hr />

        <h2>Commentaires</h2>
        <CommentListContainer />

        <hr />

        <h2>Changements</h2>
        <ScenarioChangeTable changes={this.props.scenario.changes} />

        <hr />

        <h2>Historique</h2>
        <HistoryFilterContainer />
        <ScenarioHistoryTableContainer scenarioId={this.props.scenarioId} />

      </div>
    );
  }

  loadScenarioIfNeeded(prevProps = {}) {
    const { scenarioId, onLoad } = this.props;
    if (scenarioId !== prevProps.scenarioId) {
      onLoad({ scenarioId });
    }
  }

}

ScenarioPage.propTypes = {
  onLoad: React.PropTypes.func.isRequired,
  scenarioId: React.PropTypes.string.isRequired,
  scenario: React.PropTypes.object,
};
