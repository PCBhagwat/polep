package polep.role;


import java.util.ArrayList;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;



import agentspring.role.AbstractRole;
import agentspring.role.Role;
import agentspring.role.RoleComponent;
import polep.domain.agent.EnergyProducer;
import polep.domain.agent.Regulator;
import polep.repository.BidRepository;
import polep.repository.EnergyProducerRepository;
import polep.repository.PowerPlantRepository;

/*<--- RegulatorRole: Prad ----->
Regulator controls Power plant owner:
	Throw a weighted coin (probability to control) to select owners for control (this is to simulate imperfect knowledge of the regulator)
	Compare Bid volume with installed capacity
	If Bid volume is less than installed capacity = Fine
	Fine is property of regulator
	Update Cash = Cash - Fine */

@RoleComponent
public class RegulatorRole extends AbstractRole<Regulator> implements Role<Regulator> {


	@Autowired
	EnergyProducerRepository energyProducerRepository;

	@Autowired
	PowerPlantRepository plantRepository;

	@Autowired
	BidRepository bidRepository;


	@Transactional
	public void act(Regulator regulator){


		Iterable<EnergyProducer> listofpowerplantowners = energyProducerRepository.listofpowerplantowners(getCurrentTick());

		double fine = regulator.getFine();
		double capacity = 0;


		for (EnergyProducer currentProducer:listofpowerplantowners)
		{
			// creates a coin

			BiasedCoin coin = new BiasedCoin(); 

			// Simplified, so that the checking is random every time. Thus there is no bias

			// coin.flip(currentProducer.getProbability()); 

			coin.flip(0); 
			double result = coin.getSide(); // flips coin gets value

			if (result <= 0.5){

				// Gets actual total capacity from the repository considering complete information (repository query may need correction)

				capacity = plantRepository.calculateCapacityOfOperationalPowerPlants(getCurrentTick());

				// Compares capacity with total of all bids of the producer for total tick. If it is < then fines the producer.			

				if (capacity < energyProducerRepository.calculateTotalSupplyofProducerForTime(currentProducer, getCurrentTick())){
					double cash = currentProducer.getCash() - fine;
					currentProducer.setCash(cash);

					// Changes probability value so that next time the probability of checking is higher. Currently set to zero					

					// double probability = currentProducer.getProbability() + 0;
					// currentProducer.setProbability(probability);

				}
				// Is an else next needed here to tell the program to goto to the next generator
			}
		}
	}
}


